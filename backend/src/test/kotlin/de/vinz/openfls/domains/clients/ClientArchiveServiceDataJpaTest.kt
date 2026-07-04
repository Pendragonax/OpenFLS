package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.categories.CategoryTemplateService
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
import de.vinz.openfls.domains.clients.archive.ClientArchiveActionType
import de.vinz.openfls.domains.clients.archive.ClientArchiveActor
import de.vinz.openfls.domains.clients.archive.ClientArchiveHistoryEntry
import de.vinz.openfls.domains.clients.archive.ClientArchiveService
import de.vinz.openfls.domains.clients.archive.export.ClientArchiveExportFormat
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.permissions.PermissionService
import de.vinz.openfls.domains.employees.services.UnprofessionalService
import de.vinz.openfls.domains.sponsors.Sponsor
import de.vinz.openfls.domains.sponsors.SponsorRepository
import de.vinz.openfls.exceptions.UserNotAllowedException
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest
@Import(
    ClientArchiveService::class,
    ClientService::class,
    de.vinz.openfls.domains.employees.services.EmployeeService::class,
    UnprofessionalService::class,
    PermissionService::class,
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

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var categoryTemplateService: CategoryTemplateService

    @MockitoBean
    lateinit var accessService: AccessService

    @Test
    fun archive_withLeadPermission_persistsHistoryEntry() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate))
        val actor = ClientArchiveActor(
            employeeId = 8,
            firstname = "Anna",
            lastname = "Lead",
            isAdmin = false,
            leadingInstitutionIds = listOf(institution.id!!)
        )

        // When
        val entry = clientArchiveService.archive(
            clientId = client.id,
            actionDate = LocalDate.of(2026, 5, 23),
            reason = "Archived by request",
            remark = "Initial archive",
            actor = actor
        )

        // Then
        val saved = clientRepository.findById(client.id)
        assertThat(saved).isPresent
        assertThat(saved.get().archived).isTrue
        assertThat(saved.get().archiveHistoryEntries).hasSize(1)
        assertThat(entry.actionType).isEqualTo(ClientArchiveActionType.ARCHIVE)
        assertThat(entry.executingEmployeeFirstname).isEqualTo("Anna")
        assertThat(entry.executingEmployeeLastname).isEqualTo("Lead")
    }

    @Test
    fun archive_withoutLeadPermission_throwsUserNotAllowedException() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val otherInstitution = institutionRepository.save(Institution(name = "Other", email = "x@y.z", phonenumber = "2"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate))
        val actor = ClientArchiveActor(
            employeeId = 8,
            firstname = "Anna",
            lastname = "Employee",
            isAdmin = false,
            leadingInstitutionIds = listOf(otherInstitution.id!!)
        )

        // When / Then
        assertThatThrownBy {
            clientArchiveService.archive(
                clientId = client.id,
                actionDate = LocalDate.of(2026, 5, 23),
                reason = "Archived by request",
                remark = "Initial archive",
                actor = actor
            )
        }.isInstanceOf(UserNotAllowedException::class.java)
    }

    @Test
    fun reactivate_withLeadPermission_restoresArchiveState() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate))
        val actor = ClientArchiveActor(
            employeeId = 8,
            firstname = "Anna",
            lastname = "Lead",
            isAdmin = false,
            leadingInstitutionIds = listOf(institution.id!!)
        )
        clientArchiveService.archive(
            clientId = client.id,
            actionDate = LocalDate.of(2026, 5, 23),
            reason = "Archived by request",
            remark = "Initial archive",
            actor = actor
        )

        // When
        val entry = clientArchiveService.reactivate(
            clientId = client.id,
            actionDate = LocalDate.of(2026, 5, 23),
            reason = "Client active again",
            remark = "Reactivated",
            actor = actor
        )

        // Then
        val saved = clientRepository.findById(client.id)
        assertThat(saved).isPresent
        assertThat(saved.get().archived).isFalse
        assertThat(saved.get().archiveHistoryEntries).hasSize(2)
        assertThat(entry.actionType).isEqualTo(ClientArchiveActionType.REACTIVATE)
        val history = clientArchiveService.getArchiveHistory(client.id)
        assertThat(history).hasSize(2)
        assertThat(history[0].actionType).isEqualTo(ClientArchiveActionType.REACTIVATE)
        assertThat(history[1].actionType).isEqualTo(ClientArchiveActionType.ARCHIVE)
    }

    @Test
    fun archive_withFavoritedClientPlans_removesOnlyMatchingFavorites() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor"))
        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate))
        val otherClient = clientRepository.save(Client(firstName = "Other", lastName = "Client", institution = institution, categoryTemplate = categoryTemplate))
        val archivePlanOne = assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = institution
            )
        )
        val archivePlanTwo = assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 2, 1),
                end = LocalDate.of(2026, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = institution
            )
        )
        val unrelatedPlan = assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 3, 1),
                end = LocalDate.of(2026, 12, 31),
                client = otherClient,
                sponsor = sponsor,
                institution = institution
            )
        )
        val employeeOneEntity = Employee(firstname = "Anna", lastname = "One")
        employeeOneEntity.access = EmployeeAccess(username = "annaone", password = "secret", role = 2, employee = employeeOneEntity)
        val employeeOne = employeeRepository.save(employeeOneEntity)
        val employeeTwoEntity = Employee(firstname = "Ben", lastname = "Two")
        employeeTwoEntity.access = EmployeeAccess(username = "bentwo", password = "secret", role = 2, employee = employeeTwoEntity)
        val employeeTwo = employeeRepository.save(employeeTwoEntity)
        employeeOne.assistancePlanFavorites.add(archivePlanOne)
        employeeOne.assistancePlanFavorites.add(unrelatedPlan)
        employeeTwo.assistancePlanFavorites.add(archivePlanTwo)
        employeeTwo.assistancePlanFavorites.add(unrelatedPlan)
        employeeRepository.save(employeeOne)
        employeeRepository.save(employeeTwo)
        val actor = ClientArchiveActor(
            employeeId = 8,
            firstname = "Anna",
            lastname = "Lead",
            isAdmin = false,
            leadingInstitutionIds = listOf(institution.id!!)
        )

        // When
        clientArchiveService.archive(
            clientId = client.id,
            actionDate = LocalDate.of(2026, 5, 23),
            reason = "Archived by request",
            remark = "Initial archive",
            actor = actor
        )
        clientArchiveService.reactivate(
            clientId = client.id,
            actionDate = LocalDate.of(2026, 5, 23),
            reason = "Client active again",
            remark = "Reactivated",
            actor = actor
        )

        // Then
        val savedEmployeeOne = employeeRepository.findById(employeeOne.id!!)
        val savedEmployeeTwo = employeeRepository.findById(employeeTwo.id!!)
        assertThat(savedEmployeeOne).isPresent
        assertThat(savedEmployeeTwo).isPresent
        assertThat(savedEmployeeOne.get().assistancePlanFavorites.map { it.id }).containsExactly(unrelatedPlan.id)
        assertThat(savedEmployeeTwo.get().assistancePlanFavorites.map { it.id }).containsExactly(unrelatedPlan.id)
    }

    @Test
    fun getArchiveHistory_withoutHistory_returnsEmptyList() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate))

        // When
        val history = clientArchiveService.getArchiveHistory(client.id)

        // Then
        assertThat(history).isEmpty()
    }

    @Test
    fun save_withExportHistoryEntry_persistsExportFormatAndAuditSnapshot() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate))
        client.archiveHistoryEntries.add(
            ClientArchiveHistoryEntry(
                actionType = ClientArchiveActionType.EXPORT,
                exportFormat = ClientArchiveExportFormat.JSON,
                actionDate = LocalDate.of(2026, 6, 13),
                actionTimestamp = LocalDateTime.of(2026, 6, 13, 11, 15),
                reason = "Export requested",
                remark = "JSON export",
                executingEmployeeId = 8,
                executingEmployeeFirstname = "Anna",
                executingEmployeeLastname = "Lead",
                client = client
            )
        )

        // When
        clientRepository.save(client)

        // Then
        val saved = clientRepository.findById(client.id)
        assertThat(saved).isPresent
        assertThat(saved.get().archiveHistoryEntries).hasSize(1)
        val entry = saved.get().archiveHistoryEntries.first()
        assertThat(entry.actionType).isEqualTo(ClientArchiveActionType.EXPORT)
        assertThat(entry.exportFormat).isEqualTo(ClientArchiveExportFormat.JSON)
        assertThat(entry.executingEmployeeFirstname).isEqualTo("Anna")
        assertThat(entry.executingEmployeeLastname).isEqualTo("Lead")
        assertThat(entry.actionTimestamp).isEqualTo(LocalDateTime.of(2026, 6, 13, 11, 15))
    }
}
