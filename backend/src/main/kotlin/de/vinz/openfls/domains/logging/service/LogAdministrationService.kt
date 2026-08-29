package de.vinz.openfls.domains.logging.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import de.vinz.openfls.domains.logging.dto.LogEntryDto
import de.vinz.openfls.domains.logging.dto.LogLevelDto
import de.vinz.openfls.domains.logging.dto.LogQueryDto
import de.vinz.openfls.domains.logging.dto.LogSettingsDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.name

@Service
class LogAdministrationService(
    @Value("\${openfls.logging.directory:./logs}") private val logDirectory: String
) {
    private var startupRootLevel: Level = Level.INFO
    private var startupClassLevels: Map<String, Level?> = emptyMap()
    private val entryStart = Regex("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{4}) \\[(.*)] (TRACE|DEBUG|INFO|WARN|ERROR)\\s+(.+?) - (.*)$")
    private val zone = ZoneId.systemDefault()

    @PostConstruct
    fun captureStartupLevels() {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        startupRootLevel = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).level ?: Level.INFO
        startupClassLevels = context.loggerList.filter { it.name != org.slf4j.Logger.ROOT_LOGGER_NAME }
            .associate { it.name to it.level }
    }

    fun availableDays(): List<String> = Files.list(logPath()).use { stream ->
        stream.filter { it.fileName.toString().matches(Regex("open-fls-backend\\.\\d{4}-\\d{2}-\\d{2}\\.log")) }
            .map { it.fileName.toString().removePrefix("open-fls-backend.").removeSuffix(".log") }
            .sorted { a, b -> b.compareTo(a) }.toList()
    }

    fun entries(query: LogQueryDto): List<LogEntryDto> {
        val days = selectedDays(query)
        return days.flatMap { parse(logPath().resolve("open-fls-backend.$it.log")) }
            .filter { matches(it, query) }
            .sortedByDescending { it.timestamp }
            .take(5_000)
    }

    fun deleteFrom(from: Instant?) {
        if (from == null) {
            val today = LocalDate.now(zone).toString()
            availableDays().forEach { day ->
                val file = logPath().resolve("open-fls-backend.$day.log")
                if (day == today) Files.writeString(file, "") else Files.deleteIfExists(file)
            }
            return
        }
        availableDays().forEach { day ->
            val file = logPath().resolve("open-fls-backend.$day.log")
            if (LocalDate.parse(day).isBefore(from.atZone(zone).toLocalDate())) Files.deleteIfExists(file)
            else if (LocalDate.parse(day) == from.atZone(zone).toLocalDate()) {
                // The selected entry and all entries older than it are removed.
                val kept = parse(file).filter { Instant.parse(it.timestamp).isAfter(from) }
                Files.writeString(file, kept.joinToString("\n") { format(it) }.let { if (it.isEmpty()) it else "$it\n" })
            }
        }
    }

    fun export(query: LogQueryDto): ByteArray = ByteArrayOutputStream().use { bytes ->
        ZipOutputStream(bytes).use { zip ->
            entries(query).groupBy { it.timestamp.substring(0, 10) }.forEach { (day, entries) ->
                zip.putNextEntry(ZipEntry("open-fls-backend.$day.log"))
                zip.write(entries.sortedBy { it.timestamp }.joinToString("\n") { format(it) }.toByteArray(StandardCharsets.UTF_8))
                zip.closeEntry()
            }
        }
        bytes.toByteArray()
    }

    fun settings(): LogSettingsDto {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        return LogSettingsDto(context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).level.levelStr,
            context.loggerList.filter { it.level != null && it.name != "ROOT" }.map { LogLevelDto(it.name, it.level.levelStr) }.sortedBy { it.logger })
    }

    fun setLevel(logger: String, level: String?) {
        val target = (LoggerFactory.getILoggerFactory() as LoggerContext).getLogger(if (logger == "ROOT") org.slf4j.Logger.ROOT_LOGGER_NAME else logger)
        target.level = level?.takeIf { it.isNotBlank() }?.uppercase()?.let(Level::valueOf)
    }

    fun resetLevel(logger: String) {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val target = context.getLogger(if (logger == "ROOT") org.slf4j.Logger.ROOT_LOGGER_NAME else logger)
        target.level = startupClassLevels[logger]
    }

    fun resetLevels() {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        context.loggerList.filter { it.name != org.slf4j.Logger.ROOT_LOGGER_NAME }
            .forEach { it.level = startupClassLevels[it.name] }
        context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).level = startupRootLevel
    }

    private fun selectedDays(query: LogQueryDto): List<String> {
        val all = availableDays()
        if (query.all) return all
        val today = LocalDate.now(zone)
        val from = query.from?.let(LocalDate::parse) ?: today
        val to = query.to?.let(LocalDate::parse) ?: from
        return all.filter { day -> LocalDate.parse(day) in from..to }
    }

    private fun parse(path: Path): List<LogEntryDto> {
        if (!Files.exists(path)) return emptyList()
        val result = mutableListOf<LogEntryDto>(); var current: LogEntryDto? = null
        Files.readAllLines(path).forEach { line ->
            val match = entryStart.matchEntire(line)
            if (match != null) {
                current?.let(result::add)
                current = LogEntryDto(Instant.parse(match.groupValues[1].replace(Regex("([+-]\\d{2})(\\d{2})$"), "$1:$2")).toString(), match.groupValues[3], match.groupValues[4], match.groupValues[2], match.groupValues[5])
            } else if (current != null) current = current!!.copy(message = current!!.message + "\n" + line)
        }
        current?.let(result::add); return result
    }

    private fun matches(entry: LogEntryDto, query: LogQueryDto) = listOfNotNull(query.query?.let { entry.message.contains(it, true) || entry.logger.contains(it, true) }, query.level?.let { entry.level.equals(it, true) }, query.logger?.let { entry.logger.contains(it, true) }, query.thread?.let { entry.thread.contains(it, true) }).all { it }
    private fun format(entry: LogEntryDto) = "${entry.timestamp.replace("Z", "+00:00").replace(Regex("([+-]\\d{2}):(\\d{2})$"), "$1$2")} [${entry.thread}] ${entry.level.padEnd(5)} ${entry.logger} - ${entry.message}"
    private fun logPath(): Path = Path.of(logDirectory).also { Files.createDirectories(it) }
}
