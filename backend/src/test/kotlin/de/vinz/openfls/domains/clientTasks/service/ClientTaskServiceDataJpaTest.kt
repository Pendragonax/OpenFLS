package de.vinz.openfls.domains.clientTasks.service

import de.vinz.openfls.domains.categories.entity.CategoryTemplate
import de.vinz.openfls.domains.categories.repository.CategoryTemplateRepository
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompleteRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompleteResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompletedPageResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCreateRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCreateResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskDeleteResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskResponse
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskUpdateRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskUpdateResult
import de.vinz.openfls.domains.clientTasks.entity.ClientTaskAuditAction
import de.vinz.openfls.domains.clientTasks.repository.ClientTaskAuditLogRepository
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.permissions.service.AccessService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
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

    @MockitoBean
    lateinit var clientService: ClientService

    @MockitoBean
    lateinit var employeeService: EmployeeService

    @MockitoBean
    lateinit var accessService: AccessService

    private lateinit var client: Client
    private lateinit var creator: Employee
    private lateinit var completer: Employee

    @BeforeEach
    fun setUp() {
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(
            CategoryTemplate(title = "Template", description = "", withoutClient = false))

        client = clientRepository.save(
            Client(
                firstName = "Max",
                lastName = "Mustermann",
                institution = institution,
                categoryTemplate = categoryTemplate
            )
        )
        creator = employeeRepository.save(Employee(firstname = "Anna", lastname = "Autorin"))
        completer = employeeRepository.save(Employee(firstname = "Ben", lastname = "Bearbeiter"))

        whenever(clientService.existsById(client.id)).thenReturn(true)
        whenever(clientService.getEntityById(client.id)).thenReturn(client)
        whenever(employeeService.getById(creator.id!!)).thenReturn(creator)
        whenever(employeeService.getById(completer.id!!)).thenReturn(completer)
        actAs(creator)
    }

    private fun actAs(employee: Employee) {
        whenever(accessService.getId()).thenReturn(employee.id!!)
    }

    private fun createTask(
        title: String,
        dueDate: LocalDate = LocalDate.of(2026, 3, 20),
        description: String = ""
    ): ClientTaskResponse {
        val result = clientTaskService.create(
            ClientTaskCreateRequest(clientId = client.id, title = title, description = description, dueDate = dueDate)
        )
        return (result as ClientTaskCreateResult.Success).response
    }

    private fun completeTask(id: Long, comment: String = "", completedOn: LocalDate = LocalDate.of(2026, 3, 10)) =
        clientTaskService.complete(id, ClientTaskCompleteRequest(comment = comment, completedOn = completedOn))

    @Test
    fun create_validRequest_persistsTaskAndAuditLog() {
        val dto = createTask("Hilfeplangespräch vorbereiten", description = "Unterlagen sammeln")

        assertThat(dto.title).isEqualTo("Hilfeplangespräch vorbereiten")
        assertThat(dto.done).isFalse()
        assertThat(dto.overdue).isFalse()
        assertThat(dto.createdByName).isEqualTo("Anna Autorin")

        val auditLogs = clientTaskAuditLogRepository.findAllByClientTaskIdOrderByChangedAtDesc(dto.id)
        assertThat(auditLogs).hasSize(1)
        assertThat(auditLogs.first().action).isEqualTo(ClientTaskAuditAction.CREATE)
        assertThat(auditLogs.first().actor).isEqualTo("Anna Autorin")
        assertThat(auditLogs.first().actorEmployeeId).isEqualTo(creator.id)
        assertThat(auditLogs.first().afterDueDate).isEqualTo(LocalDate.of(2026, 3, 20))
    }

    @Test
    fun create_unknownClient_returnsClientNotFound() {
        val result = clientTaskService.create(ClientTaskCreateRequest(clientId = 9999, title = "Egal"))

        assertThat(result).isEqualTo(ClientTaskCreateResult.ClientNotFound)
    }

    @Test
    fun getOpenTasksByClientId_dueDateInThePast_marksTaskAsOverdue() {
        createTask("Überfällig", dueDate = LocalDate.of(2026, 3, 1))

        val tasks = clientTaskService.getOpenTasksByClientId(client.id)

        assertThat(tasks).hasSize(1)
        assertThat(tasks!!.first().overdue).isTrue()
    }

    @Test
    fun getOpenTasksByClientId_unknownClient_returnsNull() {
        assertThat(clientTaskService.getOpenTasksByClientId(9999)).isNull()
    }

    @Test
    fun complete_openTask_storesCommentDateAndActor() {
        val created = createTask("Bericht schreiben")
        actAs(completer)

        val result = completeTask(created.id, "Bericht versendet", LocalDate.of(2026, 3, 9))

        val completed = (result as ClientTaskCompleteResult.Success).response
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
    fun complete_alreadyCompletedTask_returnsAlreadyCompleted() {
        val created = createTask("Nur einmal")
        completeTask(created.id)

        val result = completeTask(created.id, completedOn = LocalDate.of(2026, 3, 10))

        assertThat(result).isEqualTo(ClientTaskCompleteResult.AlreadyCompleted)
    }

    @Test
    fun complete_unknownTask_returnsNotFound() {
        assertThat(completeTask(9999)).isEqualTo(ClientTaskCompleteResult.NotFound)
    }

    @Test
    fun update_changedTitleAndDueDate_isRecordedWithBeforeAndAfter() {
        val created = createTask("Alter Titel")
        actAs(completer)

        clientTaskService.update(
            created.id,
            ClientTaskUpdateRequest(title = "Neuer Titel", description = "", dueDate = LocalDate.of(2026, 4, 1))
        )

        val updateLog = clientTaskAuditLogRepository
            .findAllByClientTaskIdOrderByChangedAtDesc(created.id)
            .first { it.action == ClientTaskAuditAction.UPDATE }
        assertThat(updateLog.beforeTitle).isEqualTo("Alter Titel")
        assertThat(updateLog.afterTitle).isEqualTo("Neuer Titel")
        assertThat(updateLog.beforeDueDate).isEqualTo(LocalDate.of(2026, 3, 20))
        assertThat(updateLog.afterDueDate).isEqualTo(LocalDate.of(2026, 4, 1))
        assertThat(updateLog.actor).isEqualTo("Ben Bearbeiter")
    }

    @Test
    fun update_completedTask_returnsAlreadyCompleted() {
        val created = createTask("Erledigt")
        completeTask(created.id)

        val result = clientTaskService.update(
            created.id,
            ClientTaskUpdateRequest(title = "Manipuliert", dueDate = LocalDate.of(2026, 4, 1))
        )

        assertThat(result).isEqualTo(ClientTaskUpdateResult.AlreadyCompleted)
    }

    @Test
    fun update_unknownTask_returnsNotFound() {
        val result = clientTaskService.update(9999, ClientTaskUpdateRequest(title = "Egal"))

        assertThat(result).isEqualTo(ClientTaskUpdateResult.NotFound)
    }

    @Test
    fun delete_existingTask_removesTaskAndKeepsAuditTrail() {
        val created = createTask("Zu löschen")
        actAs(completer)

        val result = clientTaskService.delete(created.id)

        assertThat((result as ClientTaskDeleteResult.Success).response.id).isEqualTo(created.id)
        assertThat(clientTaskService.getOpenTasksByClientId(client.id)).isEmpty()
        val deleteLog = clientTaskAuditLogRepository
            .findAllByClientTaskIdOrderByChangedAtDesc(created.id)
            .first { it.action == ClientTaskAuditAction.DELETE }
        assertThat(deleteLog.actor).isEqualTo("Ben Bearbeiter")
        assertThat(deleteLog.beforeTitle).isEqualTo("Zu löschen")
    }

    @Test
    fun delete_unknownTask_returnsNotFound() {
        assertThat(clientTaskService.delete(9999)).isEqualTo(ClientTaskDeleteResult.NotFound)
    }

    @Test
    fun getCompletedTasksByClientId_returnsTenItemsPerPage() {
        repeat(11) { index ->
            val created = createTask("Aufgabe $index")
            completeTask(created.id)
        }

        val first = (clientTaskService.getCompletedTasksByClientId(client.id, 0, 10)
                as ClientTaskCompletedPageResult.Success).response
        val second = (clientTaskService.getCompletedTasksByClientId(client.id, 1, 10)
                as ClientTaskCompletedPageResult.Success).response

        assertThat(first.content).hasSize(10)
        assertThat(first.totalElements).isEqualTo(11)
        assertThat(first.totalPages).isEqualTo(2)
        assertThat(second.content).hasSize(1)
    }

    @Test
    fun getCompletedTasksByClientId_invalidPagination_returnsInvalidPagination() {
        assertThat(clientTaskService.getCompletedTasksByClientId(client.id, -1, 10))
            .isEqualTo(ClientTaskCompletedPageResult.InvalidPagination)
        assertThat(clientTaskService.getCompletedTasksByClientId(client.id, 0, 0))
            .isEqualTo(ClientTaskCompletedPageResult.InvalidPagination)
        assertThat(clientTaskService.getCompletedTasksByClientId(client.id, 0, 101))
            .isEqualTo(ClientTaskCompletedPageResult.InvalidPagination)
    }

    @Test
    fun getCompletedTasksByClientId_unknownClient_returnsClientNotFound() {
        assertThat(clientTaskService.getCompletedTasksByClientId(9999, 0, 10))
            .isEqualTo(ClientTaskCompletedPageResult.ClientNotFound)
    }

    @Test
    fun getAuditHistoryByTaskId_onlyReturnsChangeAndCompleteActions() {
        val created = createTask("Alt", description = "Vorher")
        clientTaskService.update(
            created.id,
            ClientTaskUpdateRequest(title = "Neu", description = "Nachher", dueDate = LocalDate.of(2026, 3, 21))
        )
        completeTask(created.id)

        val history = clientTaskService.getAuditHistoryByTaskId(created.id)!!

        assertThat(history.map { it.action })
            .containsExactlyInAnyOrder(ClientTaskAuditAction.COMPLETE, ClientTaskAuditAction.UPDATE)
        val update = history.first { it.action == ClientTaskAuditAction.UPDATE }
        assertThat(update.beforeDescription).isEqualTo("Vorher")
        assertThat(update.afterDescription).isEqualTo("Nachher")
    }

    @Test
    fun getAuditHistoryByTaskId_unknownTask_returnsNull() {
        assertThat(clientTaskService.getAuditHistoryByTaskId(9999)).isNull()
    }

    @Test
    fun deleteAllByClientId_removesTasksAndKeepsTheAuditTrail() {
        val first = createTask("Erste")
        createTask("Zweite", dueDate = LocalDate.of(2026, 3, 21))

        clientTaskService.deleteAllByClientId(client.id, completer.id!!, "Ben Bearbeiter")

        assertThat(clientTaskService.getOpenTasksByClientId(client.id)).isEmpty()

        val deleteLogs = clientTaskAuditLogRepository
            .findAll()
            .filter { it.clientId == client.id && it.action == ClientTaskAuditAction.DELETE }
        assertThat(deleteLogs).hasSize(2)
        assertThat(deleteLogs.map { it.actor }).containsOnly("Ben Bearbeiter")
        assertThat(deleteLogs.map { it.clientTaskId }).contains(first.id)
    }

    @Test
    fun getOpenTaskCountsByClientIds_countsOpenAndOverdueTasks() {
        createTask("Offen")
        createTask("Überfällig", dueDate = LocalDate.of(2026, 3, 1))
        val done = createTask("Erledigt", dueDate = LocalDate.of(2026, 3, 2))
        completeTask(done.id, completedOn = LocalDate.of(2026, 3, 3))

        val counts = clientTaskService.getOpenTaskCountsByClientIds(listOf(client.id))

        assertThat(counts[client.id]?.openCount).isEqualTo(2)
        assertThat(counts[client.id]?.overdueCount).isEqualTo(1)
    }
}
