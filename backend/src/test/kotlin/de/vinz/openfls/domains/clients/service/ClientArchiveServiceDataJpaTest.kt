package de.vinz.openfls.domains.clients.service

import de.vinz.openfls.domains.institutions.service.InstitutionLookupService
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.repository.AssistancePlanRepository
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.categories.entity.CategoryTemplate
import de.vinz.openfls.domains.categories.repository.CategoryTemplateRepository
import de.vinz.openfls.domains.categories.service.CategoryTemplateService
import de.vinz.openfls.domains.clients.dto.ClientArchiveHistoryResult
import de.vinz.openfls.domains.clients.dto.ClientArchiveResult
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.clients.entity.ClientArchiveActionType
import de.vinz.openfls.domains.clients.entity.ClientArchiveExportFormat
import de.vinz.openfls.domains.clients.repository.ClientRepository
import de.vinz.openfls.domains.employees.dto.EmployeeNameDto
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.entity.EmployeeAccess
import de.vinz.openfls.domains.employees.repository.EmployeeRepository
import de.vinz.openfls.domains.employees.service.EmployeeAccessService
import de.vinz.openfls.domains.employees.service.EmployeeFavoriteService
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.employees.service.UnprofessionalService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.permissions.service.PermissionService
import de.vinz.openfls.domains.sponsors.entity.Sponsor
import de.vinz.openfls.domains.sponsors.repository.SponsorRepository
import de.vinz.openfls.domains.sponsors.service.SponsorService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest
@Import(
    ClientArchiveService::class,
    ClientService::class,
    EmployeeService::class,
    EmployeeFavoriteService::class,
    EmployeeAccessService::class,
    SponsorService::class,
    UnprofessionalService::class,
    PermissionService::class,
    InstitutionLookupService::class,
    TestBeans::class
)
class ClientArchiveServiceDataJpaTest {

    @Autowired
    lateinit var clientArchiveService: ClientArchiveService

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Autowired
    lateinit var categoryTemplateRepository: CategoryTemplateRepository

    @Autowired
    lateinit var sponsorRepository: SponsorRepository

    @Autowired
    lateinit var assistancePlanRepository: AssistancePlanRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var assistancePlanService: AssistancePlanService

    @MockitoBean
    lateinit var categoryTemplateService: CategoryTemplateService

    @MockitoBean
    lateinit var accessService: AccessService

    private lateinit var institution: Institution
    private lateinit var categoryTemplate: CategoryTemplate
    private lateinit var actor: Employee
    private val actionDate = LocalDate.of(2026, 5, 23)

    @BeforeEach
    fun setUp() {
        institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        categoryTemplate = categoryTemplateRepository.save(
            CategoryTemplate(title = "Template", description = "", withoutClient = false)
        )
        actor = employeeRepository.save(Employee(firstname = "Anna", lastname = "Lead"))
        whenever(accessService.getId()).thenReturn(actor.id)
        whenever(accessService.isLeader(institution.id!!)).thenReturn(true)
    }

    @Test
    fun archive_leaderOfTheInstitution_persistsHistoryEntryWithTheExecutingEmployee() {
        // Given
        val client = saveClient()

        // When
        val result = clientArchiveService.archive(client.id, actionDate, "Archived by request", "Initial archive")

        // Then
        val entry = (result as ClientArchiveResult.Success).response
        val saved = clientRepository.findById(client.id).get()
        assertThat(saved.archived).isTrue
        assertThat(saved.archiveHistoryEntries).hasSize(1)
        assertThat(entry.actionType).isEqualTo(ClientArchiveActionType.ARCHIVE)
        assertThat(entry.executingEmployeeId).isEqualTo(actor.id)
        assertThat(entry.executingEmployeeFirstname).isEqualTo("Anna")
        assertThat(entry.executingEmployeeLastname).isEqualTo("Lead")
    }

    @Test
    fun archive_withoutLeaderRights_returnsForbiddenAndKeepsTheClient() {
        // Given
        val client = saveClient()
        whenever(accessService.isLeader(institution.id!!)).thenReturn(false)

        // When
        val result = clientArchiveService.archive(client.id, actionDate, "Archived by request", "Initial archive")

        // Then
        assertThat(result).isEqualTo(ClientArchiveResult.Forbidden)
        assertThat(clientRepository.findById(client.id).get().archived).isFalse
    }

    @Test
    fun archive_unknownClient_returnsNotFound() {
        assertThat(clientArchiveService.archive(9999, actionDate, "Reason", "Remark"))
            .isEqualTo(ClientArchiveResult.NotFound)
    }

    @Test
    fun archive_executingEmployeeDoesNotExist_returnsActorNotFound() {
        // Given
        val client = saveClient()
        whenever(accessService.getId()).thenReturn(0L)

        // When / Then
        assertThat(clientArchiveService.archive(client.id, actionDate, "Reason", "Remark"))
            .isEqualTo(ClientArchiveResult.ActorNotFound)
        assertThat(clientRepository.findById(client.id).get().archived).isFalse
    }

    @Test
    fun archive_alreadyArchivedClient_returnsAlreadyArchived() {
        // Given
        val client = saveClient(archived = true)

        // When / Then
        assertThat(clientArchiveService.archive(client.id, actionDate, "Reason", "Remark"))
            .isEqualTo(ClientArchiveResult.AlreadyArchived)
    }

    @Test
    fun reactivate_activeClient_returnsNotArchived() {
        // Given
        val client = saveClient()

        // When / Then
        assertThat(clientArchiveService.reactivate(client.id, actionDate, "Reason", "Remark"))
            .isEqualTo(ClientArchiveResult.NotArchived)
    }

    @Test
    fun reactivate_archivedClient_restoresStateAndListsHistoryNewestFirst() {
        // Given
        val client = saveClient()
        clientArchiveService.archive(client.id, actionDate, "Archived by request", "Initial archive")

        // When
        val result = clientArchiveService.reactivate(client.id, actionDate, "Client active again", "Reactivated")

        // Then
        assertThat((result as ClientArchiveResult.Success).response.actionType).isEqualTo(ClientArchiveActionType.REACTIVATE)
        val saved = clientRepository.findById(client.id).get()
        assertThat(saved.archived).isFalse
        assertThat(saved.archiveHistoryEntries).hasSize(2)
        val history = (clientArchiveService.getHistory(client.id) as ClientArchiveHistoryResult.Success).entries
        assertThat(history.map { it.actionType })
            .containsExactly(ClientArchiveActionType.REACTIVATE, ClientArchiveActionType.ARCHIVE)
    }

    @Test
    fun archive_removesTheFavouritesOfTheClientOnly() {
        // Given
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor"))
        val client = saveClient()
        val otherClient = saveClient("Other", "Client")
        val archivePlanOne = savePlan(client, sponsor, LocalDate.of(2026, 1, 1))
        val archivePlanTwo = savePlan(client, sponsor, LocalDate.of(2026, 2, 1))
        val unrelatedPlan = savePlan(otherClient, sponsor, LocalDate.of(2026, 3, 1))
        val employeeOne = saveEmployee("Anna", "One", "annaone")
        val employeeTwo = saveEmployee("Ben", "Two", "bentwo")
        employeeOne.assistancePlanFavorites.addAll(listOf(archivePlanOne, unrelatedPlan))
        employeeTwo.assistancePlanFavorites.addAll(listOf(archivePlanTwo, unrelatedPlan))
        employeeOne.clientFavorites.addAll(listOf(client, otherClient))
        employeeRepository.save(employeeOne)
        employeeRepository.save(employeeTwo)

        // When
        clientArchiveService.archive(client.id, actionDate, "Archived by request", "Initial archive")

        // Then
        testEntityManager.flush()
        testEntityManager.clear()
        assertThat(employeeRepository.findById(employeeOne.id!!).get().assistancePlanFavorites.map { it.id })
            .containsExactly(unrelatedPlan.id)
        assertThat(employeeRepository.findById(employeeTwo.id!!).get().assistancePlanFavorites.map { it.id })
            .containsExactly(unrelatedPlan.id)
        assertThat(employeeRepository.findById(employeeOne.id!!).get().clientFavorites.map { it.id })
            .containsExactly(otherClient.id)
    }

    @Test
    fun getHistory_withoutHistory_returnsEmptyList() {
        // Given
        val client = saveClient()

        // When
        val result = clientArchiveService.getHistory(client.id)

        // Then
        assertThat((result as ClientArchiveHistoryResult.Success).entries).isEmpty()
    }

    @Test
    fun getHistory_withoutLeaderRights_returnsForbidden() {
        // Given
        val client = saveClient()
        whenever(accessService.isLeader(institution.id!!)).thenReturn(false)

        // When / Then
        assertThat(clientArchiveService.getHistory(client.id)).isEqualTo(ClientArchiveHistoryResult.Forbidden)
    }

    @Test
    fun getHistory_unknownClient_returnsNotFound() {
        assertThat(clientArchiveService.getHistory(9999)).isEqualTo(ClientArchiveHistoryResult.NotFound)
    }

    @Test
    fun recordExport_persistsExportFormatAndAuditSnapshot() {
        // Given
        val client = saveClient()
        val executing = EmployeeNameDto(id = 8, firstName = "Anna", lastName = "Lead")

        // When
        clientArchiveService.recordExport(
            client = client,
            actor = executing,
            actionTimestamp = LocalDateTime.of(2026, 6, 13, 11, 15),
            remark = "JSON export",
            exportFormat = ClientArchiveExportFormat.JSON
        )

        // Then
        testEntityManager.flush()
        testEntityManager.clear()
        val entry = clientRepository.findById(client.id).get().archiveHistoryEntries.single()
        assertThat(entry.actionType).isEqualTo(ClientArchiveActionType.EXPORT)
        assertThat(entry.exportFormat).isEqualTo(ClientArchiveExportFormat.JSON)
        assertThat(entry.executingEmployeeId).isEqualTo(8)
        assertThat(entry.executingEmployeeFirstname).isEqualTo("Anna")
        assertThat(entry.actionTimestamp).isEqualTo(LocalDateTime.of(2026, 6, 13, 11, 15))
        assertThat(entry.reason).isEqualTo("Export requested")
    }

    private fun saveClient(firstName: String = "Max", lastName: String = "Mustermann", archived: Boolean = false): Client =
        clientRepository.save(
            Client(
                firstName = firstName,
                lastName = lastName,
                institution = institution,
                categoryTemplate = categoryTemplate,
                archived = archived
            )
        )

    private fun savePlan(client: Client, sponsor: Sponsor, start: LocalDate): AssistancePlan =
        assistancePlanRepository.save(
            AssistancePlan(
                start = start,
                end = LocalDate.of(2026, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = institution
            )
        )

    private fun saveEmployee(firstName: String, lastName: String, username: String): Employee {
        val employee = Employee(firstname = firstName, lastname = lastName)
        employee.access = EmployeeAccess(username = username, password = "secret", role = 2, employee = employee)
        return employeeRepository.save(employee)
    }
}
