package de.vinz.openfls.domains.backup

import com.fasterxml.jackson.databind.ObjectMapper
import de.vinz.openfls.domains.backup.service.BackupStatusService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.temporal.ChronoUnit

class BackupStatusServiceTest {

    @TempDir
    lateinit var statusDir: Path

    private val objectMapper = ObjectMapper()

    private fun service(maxAgeHours: Long = 7) =
        BackupStatusService(objectMapper, statusDir.toString(), maxAgeHours)

    private fun write(name: String, content: String) {
        Files.writeString(statusDir.resolve(name), content)
    }

    private fun backupLine(timestamp: String, outcome: String = "success") =
        """{"schema_version":1,"timestamp":"$timestamp","level":"INFO","event_name":"backup.completed","outcome":"$outcome","service":"openfls-backup","message":"Verified logical MySQL backup created","run_id":"${timestamp}-openfls","database":"openfls","backup_file":"${timestamp}-openfls.sql.gz","size_bytes":1829417,"sha256":"abc123","duration_seconds":13,"reason":""}"""

    @Test
    fun status_missingFiles_reportsUnknownAndOverdue() {
        val status = service().status()

        assertThat(status.lastBackup).isNull()
        assertThat(status.lastRestoreTest).isNull()
        assertThat(status.backupOverdue).isTrue()
        assertThat(status.overall).isEqualTo("unknown")
        assertThat(status.maxAgeHours).isEqualTo(7)
    }

    @Test
    fun status_recentSuccess_isOkAndNotOverdue() {
        val now = Instant.now().minus(30, ChronoUnit.MINUTES).toString()
        write("latest.json", backupLine(now))

        val status = service().status()

        assertThat(status.overall).isEqualTo("ok")
        assertThat(status.backupOverdue).isFalse()
        assertThat(status.lastBackup?.outcome).isEqualTo("success")
        assertThat(status.lastBackup?.sizeBytes).isEqualTo(1829417)
        assertThat(status.lastBackup?.sha256).isEqualTo("abc123")
        assertThat(status.lastBackup?.durationSeconds).isEqualTo(13)
    }

    @Test
    fun status_successButTooOld_isOverdue() {
        val old = Instant.now().minus(9, ChronoUnit.HOURS).toString()
        write("latest.json", backupLine(old))

        val status = service().status()

        assertThat(status.backupOverdue).isTrue()
        assertThat(status.overall).isEqualTo("overdue")
    }

    @Test
    fun status_lastRunFailed_isFailedEvenWhenRecent() {
        val now = Instant.now().toString()
        write("latest.json", backupLine(now, outcome = "failure"))

        val status = service().status()

        assertThat(status.overall).isEqualTo("failed")
        assertThat(status.backupOverdue).isTrue()
    }

    @Test
    fun status_failedRun_exposesReason() {
        write(
            "latest.json",
            """{"timestamp":"${Instant.now()}","level":"ERROR","event_name":"backup.completed","outcome":"failure","service":"openfls-backup","message":"mysqldump denied: backup database user missing or password mismatch","run_id":"r","database":"openfls","backup_file":"r.sql.gz","size_bytes":0,"sha256":"","duration_seconds":0,"reason":"backup_user_missing"}"""
        )

        val status = service().status()

        assertThat(status.overall).isEqualTo("failed")
        assertThat(status.lastBackup?.reason).isEqualTo("backup_user_missing")
    }

    @Test
    fun status_successRun_hasNullReason() {
        write("latest.json", backupLine(Instant.now().toString()))

        assertThat(service().status().lastBackup?.reason).isNull()
    }

    @Test
    fun status_newerSchemaVersion_isStillParsedLeniently() {
        write(
            "latest.json",
            """{"schema_version":99,"timestamp":"${Instant.now()}","outcome":"success","message":"ok","backup_file":"x.sql.gz","size_bytes":10,"sha256":"h","duration_seconds":1,"reason":"","some_future_field":true}"""
        )

        val status = service().status()

        assertThat(status.overall).isEqualTo("ok")
        assertThat(status.lastBackup?.sizeBytes).isEqualTo(10)
    }

    @Test
    fun status_malformedLatestFile_isTreatedAsUnknown() {
        write("latest.json", "{ this is not valid json")

        val status = service().status()

        assertThat(status.lastBackup).isNull()
        assertThat(status.overall).isEqualTo("unknown")
    }

    @Test
    fun status_readsRestoreTestLatest() {
        val now = Instant.now().toString()
        write("latest.json", backupLine(now))
        write(
            "restore-test-latest.json",
            """{"timestamp":"$now","level":"INFO","event_name":"restore_test.completed","outcome":"success","service":"openfls-restore-test","message":"Backup was restored and the OpenFLS schema was validated","backup_file":"x.sql.gz","duration_seconds":42}"""
        )

        val status = service().status()

        assertThat(status.lastRestoreTest?.outcome).isEqualTo("success")
        assertThat(status.lastRestoreTest?.durationSeconds).isEqualTo(42)
    }

    @Test
    fun history_mergesBackupAndRestoreTestSortedByTimestampDescending() {
        write(
            "history.jsonl",
            listOf(
                backupLine("2026-08-28T02:00:00.000Z"),
                backupLine("2026-08-30T02:00:00.000Z")
            ).joinToString("\n") + "\n"
        )
        write(
            "restore-test-history.jsonl",
            """{"timestamp":"2026-08-29T03:00:00.000Z","level":"INFO","event_name":"restore_test.completed","outcome":"success","service":"openfls-restore-test","message":"ok","backup_file":"x.sql.gz","duration_seconds":30}""" + "\n"
        )

        val history = service().history(100)

        assertThat(history).hasSize(3)
        assertThat(history.map { it.timestamp }).containsExactly(
            "2026-08-30T02:00:00.000Z",
            "2026-08-29T03:00:00.000Z",
            "2026-08-28T02:00:00.000Z"
        )
        assertThat(history.first().kind).isEqualTo("backup")
        assertThat(history[1].kind).isEqualTo("restore_test")
    }

    @Test
    fun history_skipsBlankAndMalformedLinesButKeepsValidOnes() {
        write(
            "history.jsonl",
            listOf(
                backupLine("2026-08-30T02:00:00.000Z"),
                "",
                "   ",
                "{ broken",
                backupLine("2026-08-29T02:00:00.000Z")
            ).joinToString("\n") + "\n"
        )

        val history = service().history(100)

        assertThat(history).hasSize(2)
        assertThat(history.map { it.timestamp }).containsExactly(
            "2026-08-30T02:00:00.000Z",
            "2026-08-29T02:00:00.000Z"
        )
    }

    @Test
    fun history_readsObjectsConcatenatedWithoutNewlines() {
        // Reproduces the malformed file the shell job produced before the fix:
        // every entry on one physical line, no separator.
        write(
            "history.jsonl",
            backupLine("2026-08-30T13:10:15.000Z", outcome = "failure") +
                backupLine("2026-08-30T13:12:43.000Z", outcome = "failure") +
                backupLine("2026-08-30T13:15:43.000Z", outcome = "success")
        )

        val history = service().history(100)

        assertThat(history).hasSize(3)
        assertThat(history.first().timestamp).isEqualTo("2026-08-30T13:15:43.000Z")
        assertThat(history.first().outcome).isEqualTo("success")
    }

    private fun configJson(intervalDays: Int = 1) =
        """{"schema_version":1,"database":"openfls","backup_time":"02:30","timezone":"Europe/Berlin","interval_days":$intervalDays,"retry_interval_seconds":300,"retention_days":14,"history_max_entries":100,"max_age_hours":26,"stale_lock_seconds":43200,"generated_at":"2026-08-30T13:00:00.000Z"}"""

    @Test
    fun status_readsConfigJson() {
        write("latest.json", backupLine(Instant.now().toString()))
        write("config.json", configJson(intervalDays = 3))

        val config = service().status().config

        assertThat(config).isNotNull
        assertThat(config?.backupTime).isEqualTo("02:30")
        assertThat(config?.timezone).isEqualTo("Europe/Berlin")
        assertThat(config?.intervalDays).isEqualTo(3)
        assertThat(config?.retentionDays).isEqualTo(14)
        assertThat(config?.retryIntervalSeconds).isEqualTo(300)
        assertThat(config?.database).isEqualTo("openfls")
    }

    @Test
    fun status_withoutConfigJson_hasNullConfigAndNextExpectedBackup() {
        write("latest.json", backupLine(Instant.now().toString()))

        val status = service().status()
        assertThat(status.config).isNull()
        assertThat(status.nextExpectedBackup).isNull()
    }

    @Test
    fun status_nextExpectedBackup_isTheConfiguredTimeAndRespectsTheDayInterval() {
        write("latest.json", backupLine(Instant.now().toString()))
        write("config.json", configJson(intervalDays = 1))
        val daily = java.time.Instant.parse(service().status().nextExpectedBackup)

        write("config.json", configJson(intervalDays = 3))
        val everyThirdDay = java.time.Instant.parse(service().status().nextExpectedBackup)

        // Both land on 02:30 Europe/Berlin (00:30 or 01:30 UTC depending on DST).
        for (instant in listOf(daily, everyThirdDay)) {
            assertThat(instant).isAfter(java.time.Instant.now())
            assertThat(instant.atZone(java.time.ZoneOffset.UTC).hour).isIn(0, 1)
            assertThat(instant.atZone(java.time.ZoneOffset.UTC).minute).isEqualTo(30)
        }
        // interval 1 -> ~1 day out (last success was today); interval 3 -> ~3 days out.
        assertThat(daily).isBefore(java.time.Instant.now().plus(java.time.Duration.ofHours(48)))
        assertThat(everyThirdDay).isAfter(daily.plus(java.time.Duration.ofHours(24)))
        assertThat(everyThirdDay).isBefore(java.time.Instant.now().plus(java.time.Duration.ofDays(5)))
    }

    @Test
    fun history_clampsLimitToRange() {
        val lines = (1..20).joinToString("\n") { backupLine("2026-08-%02dT02:00:00.000Z".format(it)) } + "\n"
        write("history.jsonl", lines)

        assertThat(service().history(5)).hasSize(5)
        assertThat(service().history(0)).hasSize(1)
        assertThat(service().history(10_000)).hasSize(20)
    }
}
