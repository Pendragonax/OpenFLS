package de.vinz.openfls.domains.clientTasks

import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
import de.vinz.openfls.domains.clientTasks.dtos.CompleteClientTaskDto
import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskCreateDto
import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskUpdateDto
import de.vinz.openfls.domains.clientTasks.exceptions.InvalidClientTaskException
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Bean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@DataJpaTest
@Import(ClientTaskService::class, ClientTaskServiceDataJpaTest.FixedClockConfiguration::class)
class ClientTaskServiceDataJpaTest {

    @TestConfiguration
    class FixedClockConfiguration {
        @Bean
        fun clock(): Clock = Clock.fixed(Instant.parse("2026-03-10T08:00:00Z"), ZoneId.of("UTC"))
    }

    @Autowired
    lateinit var clientTaskService: ClientTaskService

    @Autowired
    lateinit var clientTaskAuditLogRepository: ClientTaskAuditLogRepository

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Autowired
    lateinit var categoryTemplateRepository: CategoryTemplateRepository

    private var clientId: Long = 0
    private var creatorId: Long = 0
    private var completerId: Long = 0

    @BeforeEach
    fun setUp() {
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(
            CategoryTemplate(title = "Template", description = "", withoutClient = false))

        clientId = clientRepository.save(
            Client(
                firstName = "Max",
                lastName = "Mustermann",
                institution = institution,
                categoryTemplate = categoryTemplate
            )
        ).id
        creatorId = employeeRepository.save(Employee(firstname = "Anna", lastname = "Autorin")).id!!
        completerId = employeeRepository.save(Employee(firstname = "Ben", lastname = "Bearbeiter")).id!!
    }

    @Test
    fun create_validDto_persistsTaskAndAuditLog() {
        val dto = clientTaskService.create(
            ClientTaskCreateDto(
                clientId = clientId,
                title = "Hilfeplangespräch vorbereiten",
                description = "Unterlagen sammeln",
                dueDate = LocalDate.of(2026, 3, 20)
            ),
            actorId = creatorId,
            actorName = "Anna Autorin"
        )

        assertThat(dto.title).isEqualTo("Hilfeplangespräch vorbereiten")
        assertThat(dto.done).isFalse()
        assertThat(dto.overdue).isFalse()
        assertThat(dto.createdByName).isEqualTo("Anna Autorin")

        val auditLogs = clientTaskAuditLogRepository.findAllByClientTaskIdOrderByChangedAtDesc(dto.id)
        assertThat(auditLogs).hasSize(1)
        assertThat(auditLogs.first().action).isEqualTo(ClientTaskAuditAction.CREATE)
        assertThat(auditLogs.first().actor).isEqualTo("Anna Autorin")
        assertThat(auditLogs.first().actorEmployeeId).isEqualTo(creatorId)
        assertThat(auditLogs.first().afterDueDate).isEqualTo(LocalDate.of(2026, 3, 20))
    }

    @Test
    fun getDtosByClientId_dueDateInThePast_marksTaskAsOverdue() {
        clientTaskService.create(
            ClientTaskCreateDto(clientId = clientId, title = "Überfällig", dueDate = LocalDate.of(2026, 3, 1)),
            actorId = creatorId,
            actorName = "Anna Autorin"
        )

        val tasks = clientTaskService.getDtosByClientId(clientId)

        assertThat(tasks).hasSize(1)
        assertThat(tasks.first().overdue).isTrue()
    }

    @Test
    fun complete_openTask_storesCommentDateAndActor() {
        val created = clientTaskService.create(
            ClientTaskCreateDto(clientId = clientId, title = "Bericht schreiben", dueDate = LocalDate.of(2026, 3, 20)),
            actorId = creatorId,
            actorName = "Anna Autorin"
        )

        val completed = clientTaskService.complete(
            id = created.id,
            valueDto = CompleteClientTaskDto(comment = "Bericht versendet", completedOn = LocalDate.of(2026, 3, 9)),
            actorId = completerId,
            actorName = "Ben Bearbeiter"
        )

        assertThat(completed.done).isTrue()
        assertThat(completed.overdue).isFalse()
        assertThat(completed.completionComment).isEqualTo("Bericht versendet")
        assertThat(completed.completedOn).isEqualTo(LocalDate.of(2026, 3, 9))
        assertThat(completed.completedByName).isEqualTo("Ben Bearbeiter")

        val auditLogs = clientTaskAuditLogRepository.findAllByClientTaskIdOrderByChangedAtDesc(created.id)
        assertThat(auditLogs.map { it.action }).contains(ClientTaskAuditAction.COMPLETE)
        val completeLog = auditLogs.first { it.action == ClientTaskAuditAction.COMPLETE }
        assertThat(completeLog.actor).isEqualTo("Ben Bearbeiter")
        assertThat(completeLog.beforeDone).isFalse()
        assertThat(completeLog.afterDone).isTrue()
        assertThat(completeLog.comment).isEqualTo("Bericht versendet")
    }

    @Test
    fun complete_alreadyCompletedTask_isRejected() {
        val created = clientTaskService.create(
            ClientTaskCreateDto(clientId = clientId, title = "Nur einmal", dueDate = LocalDate.of(2026, 3, 20)),
            actorId = creatorId,
            actorName = "Anna Autorin"
        )
        clientTaskService.complete(
            created.id,
            CompleteClientTaskDto(comment = "", completedOn = LocalDate.of(2026, 3, 9)),
            completerId,
            "Ben Bearbeiter"
        )

        assertThatThrownBy {
            clientTaskService.complete(
                created.id,
                CompleteClientTaskDto(comment = "", completedOn = LocalDate.of(2026, 3, 10)),
                completerId,
                "Ben Bearbeiter"
            )
        }.isInstanceOf(InvalidClientTaskException::class.java)
    }

    @Test
    fun update_changedTitleAndDueDate_isRecordedWithBeforeAndAfter() {
        val created = clientTaskService.create(
            ClientTaskCreateDto(clientId = clientId, title = "Alter Titel", dueDate = LocalDate.of(2026, 3, 20)),
            actorId = creatorId,
            actorName = "Anna Autorin"
        )

        clientTaskService.update(
            created.id,
            ClientTaskUpdateDto(
                title = "Neuer Titel",
                description = "",
                dueDate = LocalDate.of(2026, 4, 1)
            ),
            actorId = completerId,
            actorName = "Ben Bearbeiter"
        )

        val updateLog = clientTaskAuditLogRepository
            .findAllByClientTaskIdOrderByChangedAtDesc(created.id)
            .first { it.action == ClientTaskAuditAction.UPDATE }
        assertThat(updateLog.beforeTitle).isEqualTo("Alter Titel")
        assertThat(updateLog.afterTitle).isEqualTo("Neuer Titel")
        assertThat(updateLog.beforeDueDate).isEqualTo(LocalDate.of(2026, 3, 20))
        assertThat(updateLog.afterDueDate).isEqualTo(LocalDate.of(2026, 4, 1))
    }

    @Test
    fun update_completedTask_isRejected() {
        val created = clientTaskService.create(
            ClientTaskCreateDto(clientId = clientId, title = "Erledigt", dueDate = LocalDate.of(2026, 3, 20)),
            creatorId, "Anna Autorin"
        )
        clientTaskService.complete(
            created.id, CompleteClientTaskDto(completedOn = LocalDate.of(2026, 3, 10)),
            completerId, "Ben Bearbeiter"
        )

        assertThatThrownBy {
            clientTaskService.update(
                created.id,
                ClientTaskUpdateDto(title = "Manipuliert", dueDate = LocalDate.of(2026, 4, 1)),
                creatorId,
                "Anna Autorin"
            )
        }.isInstanceOf(InvalidClientTaskException::class.java)
    }

    @Test
    fun getCompletedDtosByClientId_returnsTenItemsPerPage() {
        repeat(11) { index ->
            val created = clientTaskService.create(
                ClientTaskCreateDto(clientId = clientId, title = "Aufgabe $index", dueDate = LocalDate.of(2026, 3, 20)),
                creatorId, "Anna Autorin"
            )
            clientTaskService.complete(
                created.id, CompleteClientTaskDto(completedOn = LocalDate.of(2026, 3, 10)),
                completerId, "Ben Bearbeiter"
            )
        }

        val first = clientTaskService.getCompletedDtosByClientId(clientId, 0)
        val second = clientTaskService.getCompletedDtosByClientId(clientId, 1)

        assertThat(first.content).hasSize(10)
        assertThat(first.totalElements).isEqualTo(11)
        assertThat(first.totalPages).isEqualTo(2)
        assertThat(second.content).hasSize(1)
    }

    @Test
    fun getAuditHistory_onlyReturnsChangeAndCompleteActions() {
        val created = clientTaskService.create(
            ClientTaskCreateDto(clientId = clientId, title = "Alt", description = "Vorher", dueDate = LocalDate.of(2026, 3, 20)),
            creatorId, "Anna Autorin"
        )
        clientTaskService.update(
            created.id,
            ClientTaskUpdateDto(title = "Neu", description = "Nachher", dueDate = LocalDate.of(2026, 3, 21)),
            creatorId,
            "Anna Autorin"
        )
        clientTaskService.complete(
            created.id, CompleteClientTaskDto(completedOn = LocalDate.of(2026, 3, 10)),
            completerId, "Ben Bearbeiter"
        )

        val history = clientTaskService.getAuditHistory(created.id)

        assertThat(history.map { it.action }).containsExactlyInAnyOrder(ClientTaskAuditAction.COMPLETE, ClientTaskAuditAction.UPDATE)
        val update = history.first { it.action == ClientTaskAuditAction.UPDATE }
        assertThat(update.beforeDescription).isEqualTo("Vorher")
        assertThat(update.afterDescription).isEqualTo("Nachher")
    }

    @Test
    fun deleteAllByClientId_removesTasksAndKeepsTheAuditTrail() {
        val first = clientTaskService.create(
            ClientTaskCreateDto(clientId = clientId, title = "Erste", dueDate = LocalDate.of(2026, 3, 20)),
            creatorId,
            "Anna Autorin"
        )
        clientTaskService.create(
            ClientTaskCreateDto(clientId = clientId, title = "Zweite", dueDate = LocalDate.of(2026, 3, 21)),
            creatorId,
            "Anna Autorin"
        )

        val removed = clientTaskService.deleteAllByClientId(clientId, completerId, "Ben Bearbeiter")

        assertThat(removed).isEqualTo(2)
        assertThat(clientTaskService.getDtosByClientId(clientId)).isEmpty()

        val deleteLogs = clientTaskAuditLogRepository
            .findAllByClientIdOrderByChangedAtDesc(clientId)
            .filter { it.action == ClientTaskAuditAction.DELETE }
        assertThat(deleteLogs).hasSize(2)
        assertThat(deleteLogs.map { it.actor }).containsOnly("Ben Bearbeiter")
        assertThat(deleteLogs.map { it.clientTaskId }).contains(first.id)
    }

    @Test
    fun getOpenTaskCountsByClientIds_countsOpenAndOverdueTasks() {
        clientTaskService.create(
            ClientTaskCreateDto(clientId = clientId, title = "Offen", dueDate = LocalDate.of(2026, 3, 20)),
            creatorId,
            "Anna Autorin"
        )
        clientTaskService.create(
            ClientTaskCreateDto(clientId = clientId, title = "Überfällig", dueDate = LocalDate.of(2026, 3, 1)),
            creatorId,
            "Anna Autorin"
        )
        val done = clientTaskService.create(
            ClientTaskCreateDto(clientId = clientId, title = "Erledigt", dueDate = LocalDate.of(2026, 3, 2)),
            creatorId,
            "Anna Autorin"
        )
        clientTaskService.complete(
            done.id,
            CompleteClientTaskDto(comment = "", completedOn = LocalDate.of(2026, 3, 3)),
            completerId,
            "Ben Bearbeiter"
        )

        val counts = clientTaskService.getOpenTaskCountsByClientIds(listOf(clientId))

        assertThat(counts[clientId]?.openCount).isEqualTo(2)
        assertThat(counts[clientId]?.overdueCount).isEqualTo(1)
    }
}
