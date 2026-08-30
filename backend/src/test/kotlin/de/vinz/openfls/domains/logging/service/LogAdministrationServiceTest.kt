package de.vinz.openfls.domains.logging.service

import de.vinz.openfls.domains.logging.dto.LogQueryDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.util.zip.ZipInputStream

class LogAdministrationServiceTest {

    @TempDir
    lateinit var logDir: Path
    private lateinit var service: LogAdministrationService
    private val day: String = LocalDate.now().toString()

    private val messageLine =
        "2026-08-30T10:00:00.000+0000 [main] ERROR d.v.o.Demo - event_name=demo.request.failed exception.type=java.lang.RuntimeException"
    private val stacktraceLines = listOf(
        "java.lang.RuntimeException: demo failed to load evaluation overview",
        "\tat de.vinz.openfls.Demo.run(Demo.kt:12)",
        "Caused by: java.lang.IllegalStateException: demo datasource connection pool exhausted",
        "\t... 1 common frames omitted"
    )
    private val plainLine =
        "2026-08-30T10:00:01.000+0000 [main] INFO  d.v.o.Demo - event_name=demo.heartbeat outcome=success"

    @BeforeEach
    fun setUp() {
        service = LogAdministrationService(logDir.toString())
    }

    private fun writeLogFile(vararg lines: String) {
        Files.writeString(logDir.resolve("open-fls-backend.$day.log"), lines.joinToString("\n") + "\n")
    }

    @Test
    fun page_exceptionEntry_separatesStacktraceFromMessageWhileKeepingMessageIntact() {
        // Given
        writeLogFile(messageLine, *stacktraceLines.toTypedArray())

        // When
        val content = service.page(LogQueryDto(all = true), 0, 100).content

        // Then
        assertThat(content).hasSize(1)
        val entry = content.single()
        assertThat(entry.level).isEqualTo("ERROR")
        assertThat(entry.message)
            .isEqualTo("event_name=demo.request.failed exception.type=java.lang.RuntimeException")
        assertThat(entry.message).doesNotContain("\n")
        assertThat(entry.stacktrace).isEqualTo(stacktraceLines.joinToString("\n"))
    }

    @Test
    fun page_plainEntry_hasNoStacktrace() {
        // Given
        writeLogFile(plainLine)

        // When
        val entry = service.page(LogQueryDto(all = true), 0, 100).content.single()

        // Then
        assertThat(entry.stacktrace).isNull()
    }

    @Test
    fun page_freeTextQuery_matchesTokenThatOnlyExistsInsideStacktrace() {
        // Given
        writeLogFile(messageLine, *stacktraceLines.toTypedArray())

        // When
        val matching = service.page(LogQueryDto(all = true, query = "connection pool exhausted"), 0, 100).content
        val notMatching = service.page(LogQueryDto(all = true, query = "no-such-token-anywhere"), 0, 100).content

        // Then
        assertThat(matching).hasSize(1)
        assertThat(notMatching).isEmpty()
    }

    @Test
    fun streamExport_keepsTheStacktraceAttachedToItsEntry() {
        // Given
        writeLogFile(messageLine, *stacktraceLines.toTypedArray())
        val buffer = ByteArrayOutputStream()

        // When
        service.streamExport(LogQueryDto(all = true), buffer)

        // Then
        val exported = firstZipEntryAsString(buffer.toByteArray())
        assertThat(exported).contains("event_name=demo.request.failed")
        stacktraceLines.forEach { assertThat(exported).contains(it) }
        assertThat(exported.indexOf("Caused by: java.lang.IllegalStateException"))
            .isGreaterThan(exported.indexOf("event_name=demo.request.failed"))
    }

    private fun firstZipEntryAsString(bytes: ByteArray): String =
        ZipInputStream(bytes.inputStream()).use { zip ->
            zip.nextEntry ?: error("export contained no zip entry")
            zip.readBytes().toString(Charsets.UTF_8)
        }
}
