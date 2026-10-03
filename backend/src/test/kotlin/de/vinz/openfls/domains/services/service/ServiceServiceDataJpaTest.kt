package de.vinz.openfls.domains.services.service

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.repository.AssistancePlanRepository
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.categories.entity.Category
import de.vinz.openfls.domains.categories.entity.CategoryTemplate
import de.vinz.openfls.domains.categories.repository.CategoryRepository
import de.vinz.openfls.domains.categories.repository.CategoryTemplateRepository
import de.vinz.openfls.domains.categories.service.CategoryTemplateService
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.employees.repository.EmployeeRepository
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.goals.repository.GoalRepository
import de.vinz.openfls.domains.goals.service.GoalService
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.hourTypes.repository.HourTypeRepository
import de.vinz.openfls.domains.hourTypes.service.HourTypeService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.permissions.service.PermissionService
import de.vinz.openfls.domains.services.dto.IdReferenceRequest
import de.vinz.openfls.domains.services.dto.ServiceCreateRequest
import de.vinz.openfls.domains.services.dto.ServiceCreateResult
import de.vinz.openfls.domains.services.dto.ServiceDeleteResult
import de.vinz.openfls.domains.services.dto.ServiceGetResult
import de.vinz.openfls.domains.services.dto.ServiceUpdateRequest
import de.vinz.openfls.domains.services.dto.ServiceUpdateResult
import de.vinz.openfls.domains.services.entity.Service
import de.vinz.openfls.domains.services.repository.ServiceRepository
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDateTime

@DataJpaTest
@Import(ServiceService::class, TestBeans::class)
class ServiceServiceDataJpaTest {

    @Autowired
    lateinit var serviceService: ServiceService

    @Autowired
    lateinit var serviceRepository: ServiceRepository

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Autowired
    lateinit var hourTypeRepository: HourTypeRepository

    @Autowired
    lateinit var assistancePlanRepository: AssistancePlanRepository

    @Autowired
    lateinit var goalRepository: GoalRepository

    @Autowired
    lateinit var categoryTemplateRepository: CategoryTemplateRepository

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    @MockitoBean
    lateinit var clientService: ClientService

    @MockitoBean
    lateinit var assistancePlanService: AssistancePlanService

    @MockitoBean
    lateinit var employeeService: EmployeeService

    @MockitoBean
    lateinit var hourTypeService: HourTypeService

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var goalService: GoalService

    @MockitoBean
    lateinit var categoryTemplateService: CategoryTemplateService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var permissionService: PermissionService

    private lateinit var institution: Institution
    private lateinit var client: Client
    private lateinit var employee: Employee
    private lateinit var otherEmployee: Employee
    private lateinit var hourType: HourType
    private lateinit var assistancePlan: AssistancePlan
    private lateinit var goal: Goal
    private lateinit var category: Category

    private val start = LocalDateTime.of(2026, 2, 1, 9, 0)
    private val end = LocalDateTime.of(2026, 2, 1, 10, 30)

    @BeforeEach
    fun setUp() {
        institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann"))
        employee = employeeRepository.save(Employee(firstname = "Anna", lastname = "Autorin"))
        otherEmployee = employeeRepository.save(Employee(firstname = "Ben", lastname = "Bearbeiter"))
        hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))
        assistancePlan = assistancePlanRepository.save(AssistancePlan(client = client, institution = institution))
        goal = goalRepository.save(Goal(title = "Ziel", assistancePlan = assistancePlan))
        val template = categoryTemplateRepository.save(
            CategoryTemplate(title = "Template", description = "", withoutClient = false)
        )
        category = categoryRepository.save(Category(title = "Kategorie", shortcut = "K", categoryTemplate = template))

        whenever(accessService.getId()).thenReturn(employee.id!!)
        whenever(accessService.isAdmin()).thenReturn(false)
        whenever(accessService.canWriteEntries(institution.id!!)).thenReturn(true)
        whenever(accessService.canReadEntries(institution.id!!)).thenReturn(true)
        whenever(employeeService.getEntityById(employee.id!!)).thenReturn(employee)
        whenever(clientService.getEntityById(client.id)).thenReturn(client)
        whenever(assistancePlanService.getEntityById(assistancePlan.id)).thenReturn(assistancePlan)
        whenever(hourTypeService.getEntityById(hourType.id)).thenReturn(hourType)
        whenever(institutionService.getEntityById(institution.id!!)).thenReturn(institution)
        whenever(goalService.getEntityById(goal.id)).thenReturn(goal)
        whenever(categoryTemplateService.getAllCategoryEntitiesByIds(listOf(category.id))).thenReturn(listOf(category))
    }

    private fun createRequest(
        clientId: Long = client.id,
        institutionId: Long = institution.id!!,
        assistancePlanId: Long = assistancePlan.id,
        hourTypeId: Long = hourType.id,
        goals: List<IdReferenceRequest> = listOf(IdReferenceRequest(goal.id)),
        categorys: List<IdReferenceRequest> = listOf(IdReferenceRequest(category.id)),
        start: LocalDateTime = this.start,
        end: LocalDateTime = this.end
    ) = ServiceCreateRequest(
        start = start, end = end, title = "Titel", content = "Inhalt", unfinished = true, groupService = true,
        clientId = clientId, institutionId = institutionId, assistancePlanId = assistancePlanId,
        hourTypeId = hourTypeId, goals = goals, categorys = categorys
    )

    private fun updateRequest(id: Long, end: LocalDateTime = this.end, institutionId: Long = institution.id!!) =
        ServiceUpdateRequest(
            id = id, start = start, end = end, title = "Neu", content = "Neuer Inhalt",
            clientId = client.id, institutionId = institutionId, assistancePlanId = assistancePlan.id,
            hourTypeId = hourType.id, goals = emptyList(), categorys = emptyList()
        )

    private fun existingService(owner: Employee = employee, clientOfService: Client = client): Service =
        serviceRepository.save(
            Service(
                start = start, end = end, minutes = 90, client = clientOfService, employee = owner,
                institution = institution, hourType = hourType, assistancePlan = assistancePlan
            )
        )

    @Test
    fun create_validRequest_persistsWithCurrentEmployeeAndCalculatesMinutes() {
        val result = serviceService.create(createRequest())

        val response = (result as ServiceCreateResult.Success).response
        val saved = serviceRepository.findById(response.id).get()
        assertThat(saved.minutes).isEqualTo(90)
        assertThat(saved.employee?.id).isEqualTo(employee.id)
        assertThat(saved.client?.id).isEqualTo(client.id)
        assertThat(saved.institution?.id).isEqualTo(institution.id)
        assertThat(saved.groupService).isTrue()
        assertThat(saved.unfinished).isTrue()
        assertThat(saved.goals.map { it.id }).containsExactly(goal.id)
        assertThat(saved.categorys.map { it.id }).containsExactly(category.id)
        assertThat(response.employeeId).isEqualTo(employee.id)
        assertThat(response.goals.map { it.id }).containsExactly(goal.id)
    }

    @Test
    fun create_withoutWriteAccess_returnsForbidden() {
        whenever(accessService.canWriteEntries(institution.id!!)).thenReturn(false)

        assertThat(serviceService.create(createRequest())).isEqualTo(ServiceCreateResult.Forbidden)
    }

    @Test
    fun create_unknownReferences_returnMatchingResult() {
        assertThat(serviceService.create(createRequest(clientId = 9999)))
            .isEqualTo(ServiceCreateResult.ClientNotFound)
        assertThat(serviceService.create(createRequest(assistancePlanId = 9999)))
            .isEqualTo(ServiceCreateResult.AssistancePlanNotFound)
        assertThat(serviceService.create(createRequest(hourTypeId = 9999)))
            .isEqualTo(ServiceCreateResult.HourTypeNotFound)
        whenever(accessService.canWriteEntries(9999)).thenReturn(true)
        assertThat(serviceService.create(createRequest(institutionId = 9999)))
            .isEqualTo(ServiceCreateResult.InstitutionNotFound)
        assertThat(serviceService.create(createRequest(goals = listOf(IdReferenceRequest(9999)))))
            .isEqualTo(ServiceCreateResult.GoalNotFound)
        assertThat(serviceService.create(createRequest(categorys = listOf(IdReferenceRequest(9999)))))
            .isEqualTo(ServiceCreateResult.CategoryNotFound)
    }

    @Test
    fun create_archivedClient_returnsClientArchived() {
        client.archived = true

        assertThat(serviceService.create(createRequest())).isEqualTo(ServiceCreateResult.ClientArchived)
    }

    @Test
    fun create_archivedAssistancePlanClient_returnsClientArchived() {
        val archivedClient = Client(id = 77, archived = true)
        assistancePlan.client = archivedClient

        assertThat(serviceService.create(createRequest())).isEqualTo(ServiceCreateResult.ClientArchived)
    }

    @Test
    fun create_endNotAfterStart_returnsInvalidTimeRange() {
        assertThat(serviceService.create(createRequest(end = start)))
            .isEqualTo(ServiceCreateResult.InvalidTimeRange)
        assertThat(serviceService.create(createRequest(end = start.minusHours(1))))
            .isEqualTo(ServiceCreateResult.InvalidTimeRange)
    }

    @Test
    fun update_ownService_updatesFieldsAndKeepsEmployee() {
        val existing = existingService()

        val result = serviceService.update(existing.id, updateRequest(existing.id, end = start.plusHours(3)))

        assertThat(result).isInstanceOf(ServiceUpdateResult.Success::class.java)
        val saved = serviceRepository.findById(existing.id).get()
        assertThat(saved.minutes).isEqualTo(180)
        assertThat(saved.title).isEqualTo("Neu")
        assertThat(saved.employee?.id).isEqualTo(employee.id)
        assertThat(saved.goals).isEmpty()
    }

    @Test
    fun update_foreignServiceAsAdmin_isAllowedAndKeepsOwner() {
        val existing = existingService(owner = otherEmployee)
        whenever(accessService.isAdmin()).thenReturn(true)

        val result = serviceService.update(existing.id, updateRequest(existing.id))

        assertThat(result).isInstanceOf(ServiceUpdateResult.Success::class.java)
        assertThat(serviceRepository.findById(existing.id).get().employee?.id).isEqualTo(otherEmployee.id)
    }

    @Test
    fun update_foreignServiceAsNonAdmin_returnsForbidden() {
        val existing = existingService(owner = otherEmployee)

        assertThat(serviceService.update(existing.id, updateRequest(existing.id)))
            .isEqualTo(ServiceUpdateResult.Forbidden)
        assertThat(serviceRepository.findById(existing.id).get().title).isEmpty()
    }

    @Test
    fun update_withoutWriteAccess_returnsForbidden() {
        val existing = existingService()
        whenever(accessService.canWriteEntries(institution.id!!)).thenReturn(false)

        assertThat(serviceService.update(existing.id, updateRequest(existing.id)))
            .isEqualTo(ServiceUpdateResult.Forbidden)
    }

    @Test
    fun update_unknownService_returnsNotFound() {
        assertThat(serviceService.update(9999, updateRequest(9999))).isEqualTo(ServiceUpdateResult.NotFound)
    }

    @Test
    fun update_archivedClient_returnsClientArchived() {
        val existing = existingService()
        client.archived = true

        assertThat(serviceService.update(existing.id, updateRequest(existing.id)))
            .isEqualTo(ServiceUpdateResult.ClientArchived)
    }

    @Test
    fun update_endBeforeStart_returnsInvalidTimeRange() {
        val existing = existingService()

        assertThat(serviceService.update(existing.id, updateRequest(existing.id, end = start.minusHours(1))))
            .isEqualTo(ServiceUpdateResult.InvalidTimeRange)
    }

    @Test
    fun delete_ownService_removesService() {
        val existing = existingService()

        val result = serviceService.delete(existing.id)

        assertThat((result as ServiceDeleteResult.Success).response.id).isEqualTo(existing.id)
        assertThat(serviceRepository.findById(existing.id)).isEmpty
    }

    @Test
    fun delete_foreignServiceAsNonAdmin_returnsForbidden() {
        val existing = existingService(owner = otherEmployee)

        assertThat(serviceService.delete(existing.id)).isEqualTo(ServiceDeleteResult.Forbidden)
        assertThat(serviceRepository.findById(existing.id)).isPresent
    }

    @Test
    fun delete_unknownService_returnsNotFound() {
        assertThat(serviceService.delete(9999)).isEqualTo(ServiceDeleteResult.NotFound)
    }

    @Test
    fun delete_archivedClient_returnsClientArchived() {
        val existing = existingService()
        client.archived = true

        assertThat(serviceService.delete(existing.id)).isEqualTo(ServiceDeleteResult.ClientArchived)
        assertThat(serviceRepository.findById(existing.id)).isPresent
    }

    @Test
    fun getById_archivedClient_populatesArchivedServiceFlag() {
        client.archived = true
        val existing = existingService()

        val result = serviceService.getById(existing.id)

        assertThat((result as ServiceGetResult.Success).response.archivedService).isTrue
    }

    @Test
    fun getById_unknownOrInvalidId_returnsNotFound() {
        assertThat(serviceService.getById(9999)).isEqualTo(ServiceGetResult.NotFound)
        assertThat(serviceService.getById(0)).isEqualTo(ServiceGetResult.NotFound)
    }

    @Test
    fun getById_withoutReadAccess_returnsForbidden() {
        val existing = existingService(owner = otherEmployee)
        whenever(accessService.canReadEntries(institution.id!!)).thenReturn(false)

        assertThat(serviceService.getById(existing.id)).isEqualTo(ServiceGetResult.Forbidden)
    }

    @Test
    fun getById_adminWithoutExplicitReadAccess_returnsService() {
        val existing = existingService(owner = otherEmployee)
        whenever(accessService.canReadEntries(institution.id!!)).thenReturn(false)
        whenever(accessService.isAdmin()).thenReturn(true)

        assertThat(serviceService.getById(existing.id)).isInstanceOf(ServiceGetResult.Success::class.java)
    }

    @Test
    fun getServicesByAssistancePlanId_unknownPlan_returnsNull() {
        whenever(assistancePlanService.existsById(9999)).thenReturn(false)

        assertThat(serviceService.getServicesByAssistancePlanId(9999)).isNull()
        assertThat(serviceService.getServicesByAssistancePlanId(0)).isNull()
    }

    @Test
    fun getServicesByAssistancePlanId_knownPlan_returnsServicesOfThePlan() {
        existingService()
        whenever(assistancePlanService.existsById(assistancePlan.id)).thenReturn(true)

        val result = serviceService.getServicesByAssistancePlanId(assistancePlan.id)

        assertThat(result).hasSize(1)
        assertThat(result!!.first().assistancePlanId).isEqualTo(assistancePlan.id)
    }

    @Test
    fun getAllEntitiesByClientId_returnsEntriesOrderedByStart() {
        val later = serviceRepository.save(
            Service(start = start.plusDays(1), end = end.plusDays(1), client = client, employee = employee)
        )
        val earlier = existingService()

        val result = serviceService.getAllEntitiesByClientId(client.id)

        assertThat(result.map { it.id }).containsExactly(earlier.id, later.id)
    }

    @Test
    fun getContingentEvaluationServicesByInstitutionIdAndYear_mapsMinutesStartAndEmployee() {
        existingService()

        val result = serviceService.getContingentEvaluationServicesByInstitutionIdAndYear(institution.id!!, 2026)

        assertThat(result).hasSize(1)
        assertThat(result.first().employeeId).isEqualTo(employee.id)
        assertThat(result.first().minutes).isEqualTo(90)
        assertThat(result.first().start).isEqualTo(start)
    }

    @Test
    fun getCalendarServicesByEmployeeIdAndStartAndEnd_mapsMinutesAndStart() {
        existingService()

        val result = serviceService.getCalendarServicesByEmployeeIdAndStartAndEnd(
            employee.id!!, start.toLocalDate().minusDays(1), start.toLocalDate().plusDays(1)
        )

        assertThat(result).hasSize(1)
        assertThat(result.first().minutes).isEqualTo(90)
    }

    @Test
    fun getMinutesInPlanPeriodByAssistancePlanIdsAndStartAndEnd_returnsMinutesPerPlan() {
        assistancePlan.start = start.toLocalDate().minusDays(5)
        assistancePlan.end = start.toLocalDate().plusDays(5)
        existingService()

        val result = serviceService.getMinutesInPlanPeriodByAssistancePlanIdsAndStartAndEnd(
            listOf(assistancePlan.id), start.toLocalDate().minusDays(1), start.toLocalDate().plusDays(1)
        )

        assertThat(result.map { it.assistancePlanId to it.minutes }).containsExactly(assistancePlan.id to 90)
    }

    @Test
    fun getServicesByAssistancePlanIdAndHourTypeIdAndYearAndMonth_returnsFlatEntries() {
        existingService()

        val result = serviceService.getServicesByAssistancePlanIdAndHourTypeIdAndYearAndMonth(
            assistancePlan.id, hourType.id, 2026, 2
        )

        assertThat(result).hasSize(1)
        assertThat(result.first().minutes).isEqualTo(90)
        assertThat(serviceService.getServicesByAssistancePlanIdAndHourTypeIdAndYearAndMonth(
            assistancePlan.id, hourType.id, 2026, 3
        )).isEmpty()
    }

    @Test
    fun countServices_countsByEmployeeClientAndAssistancePlan() {
        existingService()

        assertThat(serviceService.countServicesByEmployeeId(employee.id!!)).isEqualTo(1)
        assertThat(serviceService.countServicesByClientId(client.id)).isEqualTo(1)
        assertThat(serviceService.countServicesByAssistancePlanId(assistancePlan.id)).isEqualTo(1)
        assertThat(serviceService.countServicesByEmployeeId(otherEmployee.id!!)).isZero()
    }

    @Test
    fun getClientServicesByDate_matchingDate_returnsFormattedEntries() {
        existingService(owner = otherEmployee)
        serviceRepository.save(
            Service(start = start.plusDays(1), end = end.plusDays(1), client = client, employee = otherEmployee)
        )

        val result = serviceService.getClientServicesByDate(client.id, start.toLocalDate())

        assertThat(result.clientId).isEqualTo(client.id)
        assertThat(result.services).hasSize(1)
        assertThat(result.services.first().timepoint).isEqualTo("09:00 - 10:30")
        assertThat(result.services.first().employeeName).isEqualTo("B. Bearbeiter")
    }

    @Test
    fun getClientServicesByDate_noMatchingDate_returnsEmptyList() {
        existingService()

        val result = serviceService.getClientServicesByDate(client.id, start.toLocalDate().plusDays(5))

        assertThat(result.services).isEmpty()
    }

    @Test
    fun getServicesByEmployeeIdAndStartAndEnd_adminSeesEntriesWithEnd() {
        whenever(accessService.isAdmin()).thenReturn(true)
        existingService(owner = otherEmployee)

        val result = serviceService.getServicesByEmployeeIdAndStartAndEnd(
            otherEmployee.id!!, start.toLocalDate().minusDays(1), start.toLocalDate().plusDays(1)
        )

        assertThat(result).hasSize(1)
        assertThat(result.first().start).isEqualTo(start)
        assertThat(result.first().end).isEqualTo(end)
        assertThat(result.first().archivedService).isFalse
    }

    @Test
    fun getServicesByEmployeeIdAndStartAndEnd_archivedClient_populatesArchivedServiceFlag() {
        whenever(accessService.isAdmin()).thenReturn(true)
        val archivedClient = clientRepository.save(Client(firstName = "Archiv", lastName = "iert", archived = true))
        existingService(clientOfService = archivedClient)

        val result = serviceService.getServicesByEmployeeIdAndStartAndEnd(
            employee.id!!, start.toLocalDate().minusDays(1), start.toLocalDate().plusDays(1)
        )

        assertThat(result.single().archivedService).isTrue
    }

    @Test
    fun getServicesByEmployeeIdAndStartAndEnd_nonAdmin_hidesForeignEntriesOutsideLeadingInstitutions() {
        existingService(owner = otherEmployee)
        whenever(permissionService.getLeadingInstitutionIdsByEmployee(employee.id!!)).thenReturn(emptyList())

        val hidden = serviceService.getServicesByEmployeeIdAndStartAndEnd(
            otherEmployee.id!!, start.toLocalDate().minusDays(1), start.toLocalDate().plusDays(1)
        )

        assertThat(hidden).isEmpty()
    }

    @Test
    fun getServicesByEmployeeIdAndStartAndEnd_nonAdmin_showsForeignEntriesOfLeadingInstitutionsAndOwnEntries() {
        existingService(owner = otherEmployee)
        existingService(owner = employee)
        whenever(permissionService.getLeadingInstitutionIdsByEmployee(employee.id!!))
            .thenReturn(listOf(institution.id!!))

        val foreign = serviceService.getServicesByEmployeeIdAndStartAndEnd(
            otherEmployee.id!!, start.toLocalDate().minusDays(1), start.toLocalDate().plusDays(1)
        )
        val own = serviceService.getServicesByEmployeeIdAndStartAndEnd(
            employee.id!!, start.toLocalDate().minusDays(1), start.toLocalDate().plusDays(1)
        )

        assertThat(foreign).hasSize(1)
        assertThat(own).hasSize(1)
    }
}
