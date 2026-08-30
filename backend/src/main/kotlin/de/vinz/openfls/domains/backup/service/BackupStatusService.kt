package de.vinz.openfls.domains.backup.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.vinz.openfls.domains.backup.dto.BackupConfigDto
import de.vinz.openfls.domains.backup.dto.BackupHistoryEntryDto
import de.vinz.openfls.domains.backup.dto.BackupRunDto
import de.vinz.openfls.domains.backup.dto.BackupStatusDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

/**
 * Reads the operational status of the external backup service from its status
 * files. Mirrors [de.vinz.openfls.domains.logging.service.LogAdministrationService]
 * in being purely file-backed: no JPA, no writes, no access to the dump files
 * themselves. The status files are the deliberate source of truth so the view
 * stays available while the database is being restored.
 */
@Service
class BackupStatusService(
    private val objectMapper: ObjectMapper,
    @param:Value("\${openfls.backup.status-directory:./backup/status}")
    private val statusDirectory: String,
    @param:Value("\${openfls.backup.max-age-hours:7}")
    private val maxAgeHours: Long
) {
    private val logger = LoggerFactory.getLogger(BackupStatusService::class.java)

    private companion object {
        const val BACKUP_LATEST = "latest.json"
        const val RESTORE_TEST_LATEST = "restore-test-latest.json"
        const val BACKUP_HISTORY = "history.jsonl"
        const val RESTORE_TEST_HISTORY = "restore-test-history.jsonl"
        const val BACKUP_CONFIG = "config.json"
        const val HISTORY_MIN_LIMIT = 1
        const val HISTORY_MAX_LIMIT = 1000

        /**
         * Highest status-file `schema_version` this reader understands. Files
         * without the field are treated as version 1. A higher version is still
         * parsed leniently but logged, so a producer/consumer mismatch after a
         * partial upgrade is visible.
         */
        const val SUPPORTED_SCHEMA_VERSION = 1L
    }

    fun status(): BackupStatusDto {
        val lastBackup = readLatest(BACKUP_LATEST)
        val lastRestoreTest = readLatest(RESTORE_TEST_LATEST)
        val overdue = isOverdue(lastBackup)
        val overall = when {
            lastBackup == null -> "unknown"
            lastBackup.outcome != "success" -> "failed"
            overdue -> "overdue"
            else -> "ok"
        }
        return BackupStatusDto(lastBackup, lastRestoreTest, overdue, maxAgeHours, overall, readConfig())
    }

    fun history(limit: Int): List<BackupHistoryEntryDto> {
        val safeLimit = limit.coerceIn(HISTORY_MIN_LIMIT, HISTORY_MAX_LIMIT)
        val entries = readHistory(BACKUP_HISTORY, "backup") + readHistory(RESTORE_TEST_HISTORY, "restore_test")
        return entries.sortedByDescending { it.timestamp ?: "" }.take(safeLimit)
    }

    private fun isOverdue(lastBackup: BackupRunDto?): Boolean {
        if (lastBackup?.outcome != "success") return true
        val instant = lastBackup.timestamp?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return true
        return Duration.between(instant, Instant.now()) > Duration.ofHours(maxAgeHours)
    }

    private fun statusPath(): Path = Path.of(statusDirectory)

    private fun readLatest(fileName: String): BackupRunDto? {
        val node = readSingleObject(fileName) ?: return null
        return toRun(node)
    }

    private fun readConfig(): BackupConfigDto? {
        val node = readSingleObject(BACKUP_CONFIG) ?: return null
        return BackupConfigDto(
            database = node.textOrNull("database"),
            intervalSeconds = node.longOrNull("interval_seconds"),
            retryIntervalSeconds = node.longOrNull("retry_interval_seconds"),
            retentionDays = node.longOrNull("retention_days"),
            historyMaxEntries = node.longOrNull("history_max_entries"),
            maxAgeHours = node.longOrNull("max_age_hours"),
            staleLockSeconds = node.longOrNull("stale_lock_seconds"),
            generatedAt = node.textOrNull("generated_at")
        )
    }

    private fun readSingleObject(fileName: String): JsonNode? {
        val file = statusPath().resolve(fileName)
        if (!Files.isReadable(file)) return null
        return runCatching {
            Files.newBufferedReader(file).use { objectMapper.readTree(it) }?.takeIf { it.isObject }
        }.getOrElse {
            logger.warn("event_name=backup.status.parse_failed outcome=failure file={}", fileName)
            null
        }?.also { warnOnNewerSchema(it, fileName) }
    }

    /** Logs once per read when a status object declares a schema newer than supported. */
    private fun warnOnNewerSchema(node: JsonNode, fileName: String) {
        val version = node.longOrNull("schema_version") ?: return
        if (version > SUPPORTED_SCHEMA_VERSION) {
            logger.warn(
                "event_name=backup.schema.unsupported outcome=failure file={} schema_version={} supported={}",
                fileName, version, SUPPORTED_SCHEMA_VERSION
            )
        }
    }

    /**
     * Reads a technical history file. Accepts proper one-object-per-line JSONL,
     * objects concatenated without a separator (a malformed file from an older
     * backup job), and a mix with individual broken lines - each salvageable
     * object is returned, newest-first ordering is applied by the caller.
     */
    private fun readHistory(fileName: String, kind: String): List<BackupHistoryEntryDto> {
        val file = statusPath().resolve(fileName)
        if (!Files.isReadable(file)) return emptyList()
        val text = runCatching { Files.readString(file) }.getOrElse {
            logger.warn("event_name=backup.history.parse_failed outcome=failure file={}", fileName)
            return emptyList()
        }
        return parseJsonObjects(text)
            .filter { it.isObject }
            .map { toHistoryEntry(it, kind) }
    }

    private fun parseJsonObjects(text: String): List<JsonNode> =
        runCatching { readAllValues(text) }.getOrElse {
            text.lineSequence()
                .flatMap { line -> runCatching { readAllValues(line) }.getOrDefault(emptyList()).asSequence() }
                .toList()
        }

    private fun readAllValues(content: String): List<JsonNode> =
        objectMapper.readerFor(JsonNode::class.java).readValues<JsonNode>(content).readAll()

    private fun toRun(node: JsonNode) = BackupRunDto(
        timestamp = node.textOrNull("timestamp"),
        outcome = node.textOrNull("outcome"),
        message = node.textOrNull("message"),
        backupFile = node.textOrNull("backup_file"),
        sizeBytes = node.longOrNull("size_bytes"),
        sha256 = node.textOrNull("sha256"),
        durationSeconds = node.longOrNull("duration_seconds"),
        reason = node.textOrNull("reason")
    )

    private fun toHistoryEntry(node: JsonNode, kind: String) = BackupHistoryEntryDto(
        kind = kind,
        timestamp = node.textOrNull("timestamp"),
        outcome = node.textOrNull("outcome"),
        message = node.textOrNull("message"),
        backupFile = node.textOrNull("backup_file"),
        sizeBytes = node.longOrNull("size_bytes"),
        sha256 = node.textOrNull("sha256"),
        durationSeconds = node.longOrNull("duration_seconds")
    )

    private fun JsonNode.textOrNull(field: String): String? =
        get(field)?.takeIf { it.isTextual && it.asText().isNotBlank() }?.asText()

    private fun JsonNode.longOrNull(field: String): Long? =
        get(field)?.takeIf { it.isNumber }?.asLong()
}
