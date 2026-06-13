package de.vinz.openfls.domains.clients.archive.export

import com.fasterxml.jackson.databind.ObjectMapper
import de.vinz.openfls.TimeConfiguration
import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.categories.entities.Category
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.repositories.CategoryRepository
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
import de.vinz.openfls.domains.categories.CategoryTemplateService
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.clients.archive.ClientArchiveActor
import de.vinz.openfls.domains.clients.archive.ClientArchiveService
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.goals.entities.GoalHour
import de.vinz.openfls.domains.goals.repositories.GoalRepository
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.hourTypes.HourTypeRepository
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.domains.permissions.PermissionService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.sponsors.Sponsor
import de.vinz.openfls.domains.sponsors.SponsorRepository
import de.vinz.openfls.domains.services.Service
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest
@Import(
    TimeConfiguration::class,
    ClientArchiveExportService::class,
    ClientArchiveService::class,
    de.vinz.openfls.domains.clients.ClientService::class,
    EmployeeService::class,
    de.vinz.openfls.domains.employees.services.UnprofessionalService::class,
    InstitutionService::class,
    CategoryTemplateService::class,
    PermissionService::class,
    TestBeans::class
)
class ClientArchiveExportServiceDataJpaTest {

    @Autowired
    lateinit var clientArchiveExportService: ClientArchiveExportService

    @Autowired
    lateinit var clientArchiveExportRequestRepository: ClientArchiveExportRequestRepository

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Autowired
    lateinit var categoryTemplateRepository: CategoryTemplateRepository

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    @Autowired
    lateinit var sponsorRepository: SponsorRepository

    @Autowired
    lateinit var assistancePlanRepository: AssistancePlanRepository

    @Autowired
    lateinit var goalRepository: GoalRepository

    @Autowired
    lateinit var serviceRepository: ServiceRepository

    @Autowired
    lateinit var hourTypeRepository: HourTypeRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var accessService: AccessService

    @Test
    fun requestExport_withNestedReferences_writesJsonAndAuditHistory() {
        // Given
        val graph = createExportGraph()
        val actor = ClientArchiveActor(
            employeeId = graph.employee.id!!,
            firstname = graph.employee.firstname,
            lastname = graph.employee.lastname,
            isAdmin = true,
            leadingInstitutionIds = emptyList()
        )

        // When
        val status = clientArchiveExportService.requestExport(
            clientId = graph.client.id,
            format = ClientArchiveExportFormat.JSON,
            actor = actor
        )

        // Then
        assertThat(status.ready).isTrue
        assertThat(status.downloadLink).isNotNull

        val request = clientArchiveExportRequestRepository.findTopByClientIdOrderByRequestedAtDesc(graph.client.id)
        assertThat(request).isNotNull
        assertThat(Files.exists(Path.of(request!!.filePath))).isTrue

        val json = objectMapper.readTree(Files.readString(Path.of(request.filePath)))
        val serviceNode = json["services"][0]
        assertThat(json["client"]["firstName"].asText()).isEqualTo("Max")
        assertThat(serviceNode["employee"]["firstName"].asText()).isEqualTo("Anna")
        assertThat(serviceNode["institution"]["name"].asText()).isEqualTo("Institution")
        assertThat(serviceNode["hourType"]["title"].asText()).isEqualTo("Service Hour")
        assertThat(serviceNode["assistancePlan"]["sponsor"]["name"].asText()).isEqualTo("Sponsor")
        assertThat(serviceNode["assistancePlan"]["goals"][0]["title"].asText()).isEqualTo("Goal Title")
        assertThat(serviceNode["assistancePlan"]["goals"][0]["evaluations"][0]["createdBy"]["firstName"].asText()).isEqualTo("Anna")
        assertThat(serviceNode["employee"].has("email")).isFalse
        assertThat(serviceNode["institution"].has("email")).isFalse
        assertThat(serviceNode["hourType"].has("price")).isFalse
        assertThat(serviceNode["categories"][0].has("description")).isFalse
        assertThat(serviceNode["assistancePlan"]["sponsor"].has("payExact")).isFalse
        assertThat(serviceNode["assistancePlan"]["goals"][0].has("institution")).isFalse

        val savedClient = clientRepository.findById(graph.client.id)
        assertThat(savedClient).isPresent
        assertThat(savedClient.get().archiveHistoryEntries).hasSize(1)
        val historyEntry = savedClient.get().archiveHistoryEntries.first()
        assertThat(historyEntry.actionType).isEqualTo(de.vinz.openfls.domains.clients.archive.ClientArchiveActionType.EXPORT)
        assertThat(historyEntry.exportFormat).isEqualTo(ClientArchiveExportFormat.JSON)
    }

    @Test
    fun downloadExport_withValidToken_removesFileAndRequest() {
        // Given
        val graph = createExportGraph()
        val actor = ClientArchiveActor(
            employeeId = graph.employee.id!!,
            firstname = graph.employee.firstname,
            lastname = graph.employee.lastname,
            isAdmin = true,
            leadingInstitutionIds = emptyList()
        )
        val status = clientArchiveExportService.requestExport(
            clientId = graph.client.id,
            format = ClientArchiveExportFormat.JSON,
            actor = actor
        )
        val token = status.downloadLink!!.downloadLink.substringAfterLast("/")
        val request = clientArchiveExportRequestRepository.findTopByClientIdOrderByRequestedAtDesc(graph.client.id)!!

        // When
        val download = clientArchiveExportService.downloadExport(graph.client.id, token)

        // Then
        assertThat(download.fileName).isEqualTo(request.fileName)
        assertThat(download.content).isNotEmpty
        assertThat(Files.exists(Path.of(request.filePath))).isFalse
        assertThat(clientArchiveExportRequestRepository.findByClientIdAndDownloadToken(graph.client.id, token)).isNull()
    }

    @Test
    fun getExportStatus_withExpiredRequest_returnsEmptyStatus() {
        // Given
        val graph = createExportGraph()
        val actor = ClientArchiveActor(
            employeeId = graph.employee.id!!,
            firstname = graph.employee.firstname,
            lastname = graph.employee.lastname,
            isAdmin = true,
            leadingInstitutionIds = emptyList()
        )
        clientArchiveExportService.requestExport(
            clientId = graph.client.id,
            format = ClientArchiveExportFormat.JSON,
            actor = actor
        )
        val request = clientArchiveExportRequestRepository.findTopByClientIdOrderByRequestedAtDesc(graph.client.id)!!
        request.expiresAt = LocalDateTime.now().minusMinutes(1)
        clientArchiveExportRequestRepository.save(request)

        // When
        val status = clientArchiveExportService.getExportStatus(graph.client.id, actor)

        // Then
        assertThat(status.ready).isFalse
        assertThat(status.downloadLink).isNull()
        assertThat(clientArchiveExportRequestRepository.findTopByClientIdOrderByRequestedAtDesc(graph.client.id)).isNull()
    }

    private fun createExportGraph(): ExportGraph {
        val institution = institutionRepository.save(
            Institution(name = "Institution", email = "institution@example.com", phonenumber = "123")
        )
        val categoryTemplate = categoryTemplateRepository.save(
            CategoryTemplate(title = "Template", description = "Template", withoutClient = false)
        )
        val client = clientRepository.save(
            Client(
                firstName = "Max",
                lastName = "Mustermann",
                institution = institution,
                categoryTemplate = categoryTemplate
            )
        )
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor"))
        val hourTypeService = hourTypeRepository.save(HourType(title = "Service Hour", price = 12.5))
        val hourTypePlan = hourTypeRepository.save(HourType(title = "Plan Hour", price = 10.0))
        val hourTypeGoal = hourTypeRepository.save(HourType(title = "Goal Hour", price = 8.0))
        val category = categoryRepository.save(
            Category(
                title = "Category",
                shortcut = "CAT",
                description = "Category description",
                faceToFace = true,
                categoryTemplate = categoryTemplate
            )
        )
        val employee = Employee(
            firstname = "Anna",
            lastname = "Lead",
            phonenumber = "12345",
            email = "anna@example.com"
        ).apply {
            access = EmployeeAccess(
                username = "annalead",
                password = "secret!",
                role = 3,
                employee = this
            )
        }
        val savedEmployee = employeeRepository.save(employee)

        val assistancePlan = assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = institution,
                hours = mutableSetOf(
                    AssistancePlanHour(
                        weeklyMinutes = 120,
                        hourType = hourTypePlan
                    )
                )
            )
        )

        val goal = goalRepository.save(
            Goal(
                title = "Goal Title",
                description = "Goal Description",
                institution = institution,
                assistancePlan = assistancePlan,
                hours = mutableSetOf(
                    GoalHour(
                        weeklyMinutes = 60,
                        hourType = hourTypeGoal
                    )
                )
            )
        )
        assistancePlan.goals.add(goal)
        assistancePlanRepository.save(assistancePlan)

        goal.evaluations.add(
            de.vinz.openfls.domains.evaluations.Evaluation(
                date = LocalDate.of(2026, 6, 13),
                content = "Evaluation content",
                approved = true,
                createdAt = LocalDateTime.of(2026, 6, 13, 10, 0),
                updatedAt = LocalDateTime.of(2026, 6, 13, 10, 5),
                createdBy = savedEmployee,
                updatedBy = savedEmployee,
                goal = goal
            )
        )
        goalRepository.save(goal)

        val service = serviceRepository.save(
            Service(
                start = LocalDateTime.of(2026, 6, 13, 11, 0),
                end = LocalDateTime.of(2026, 6, 13, 12, 0),
                minutes = 60,
                title = "Service Title",
                content = "Service Content",
                groupService = false,
                unfinished = false,
                client = client,
                employee = savedEmployee,
                institution = institution,
                hourType = hourTypeService,
                assistancePlan = assistancePlan,
                goals = mutableSetOf(goal),
                categorys = mutableSetOf(category)
            )
        )

        return ExportGraph(client = client, employee = savedEmployee)
    }

    private data class ExportGraph(
        val client: Client,
        val employee: Employee
    )

    @TestConfiguration
    class JsonTestConfig {
        @Bean
        @Primary
        fun objectMapper(): ObjectMapper = ObjectMapper().findAndRegisterModules()
    }
}
