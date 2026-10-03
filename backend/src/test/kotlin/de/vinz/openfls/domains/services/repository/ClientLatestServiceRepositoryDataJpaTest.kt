package de.vinz.openfls.domains.services.repository

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.categories.entity.CategoryTemplate
import de.vinz.openfls.domains.categories.repository.CategoryTemplateRepository
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.employees.repository.EmployeeRepository
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.hourTypes.repository.HourTypeRepository
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.services.entity.Service
import de.vinz.openfls.domains.sponsors.entity.Sponsor
import de.vinz.openfls.domains.sponsors.repository.SponsorRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * The dashboard shows the latest entries of a client. The query must never hand out
 * entries of institutions the employee may not read - own entries stay visible.
 */
@DataJpaTest
class ClientLatestServiceRepositoryDataJpaTest {

    @Autowired
    lateinit var serviceRepository: ServiceRepository

    @Autowired
    lateinit var assistancePlanRepository: AssistancePlanRepository

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Autowired
    lateinit var categoryTemplateRepository: CategoryTemplateRepository

    @Autowired
    lateinit var sponsorRepository: SponsorRepository

    @Autowired
    lateinit var hourTypeRepository: HourTypeRepository

    private lateinit var readableInstitution: Institution
    private lateinit var foreignInstitution: Institution
    private lateinit var client: Client
    private lateinit var requestingEmployee: Employee
    private lateinit var foreignEmployee: Employee
    private lateinit var readablePlan: AssistancePlan
    private lateinit var foreignPlan: AssistancePlan
    private lateinit var hourType: HourType

    @BeforeEach
    fun setUp() {
        readableInstitution = institutionRepository.save(Institution(name = "Lesbar", email = "a@b.c", phonenumber = "1"))
        foreignInstitution = institutionRepository.save(Institution(name = "Fremd", email = "d@e.f", phonenumber = "2"))
        val categoryTemplate = categoryTemplateRepository.save(
            CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor"))
        hourType = hourTypeRepository.save(HourType(title = "Fachleistung", price = 1.0))

        client = clientRepository.save(
            Client(
                firstName = "Max",
                lastName = "Mustermann",
                institution = readableInstitution,
                categoryTemplate = categoryTemplate
            )
        )
        requestingEmployee = employeeRepository.save(Employee(firstname = "Anna", lastname = "Anfragend"))
        foreignEmployee = employeeRepository.save(Employee(firstname = "Ben", lastname = "Fremd"))

        readablePlan = assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = readableInstitution
            )
        )
        foreignPlan = assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = foreignInstitution
            )
        )
    }

    @Test
    fun findLatestClientServiceDtos_returnsNewestEntriesFirstAndRespectsLimit() {
        (1..7).forEach { day ->
            saveService(
                day = day,
                title = "Eintrag $day",
                employee = requestingEmployee,
                institution = readableInstitution,
                assistancePlan = readablePlan
            )
        }

        val result = serviceRepository.findLatestByClientId(
            clientId = client.id,
            employeeId = requestingEmployee.id!!,
            readableInstitutionIds = listOf(readableInstitution.id!!),
            isAdmin = false,
            pageable = PageRequest.of(0, 5)
        )

        assertThat(result).hasSize(5)
        assertThat(result.map { it.title }).containsExactly(
            "Eintrag 7", "Eintrag 6", "Eintrag 5", "Eintrag 4", "Eintrag 3")
        assertThat(result.first().employeeFirstname).isEqualTo("Anna")
        assertThat(result.first().institutionName).isEqualTo("Lesbar")
        assertThat(result.first().assistancePlanId).isEqualTo(readablePlan.id)
    }

    @Test
    fun findLatestClientServiceDtos_hidesForeignInstitutionEntriesButKeepsOwnEntries() {
        saveService(1, "Fremde Einrichtung, fremder Mitarbeiter", foreignEmployee, foreignInstitution, foreignPlan)
        saveService(2, "Fremde Einrichtung, eigener Eintrag", requestingEmployee, foreignInstitution, foreignPlan)
        saveService(3, "Lesbare Einrichtung", foreignEmployee, readableInstitution, readablePlan)

        val result = serviceRepository.findLatestByClientId(
            clientId = client.id,
            employeeId = requestingEmployee.id!!,
            readableInstitutionIds = listOf(readableInstitution.id!!),
            isAdmin = false,
            pageable = PageRequest.of(0, 5)
        )

        assertThat(result.map { it.title })
            .containsExactly("Lesbare Einrichtung", "Fremde Einrichtung, eigener Eintrag")
    }

    @Test
    fun findLatestClientServiceDtos_adminSeesEveryEntry() {
        saveService(1, "Fremde Einrichtung", foreignEmployee, foreignInstitution, foreignPlan)
        saveService(2, "Lesbare Einrichtung", foreignEmployee, readableInstitution, readablePlan)

        val result = serviceRepository.findLatestByClientId(
            clientId = client.id,
            employeeId = requestingEmployee.id!!,
            readableInstitutionIds = listOf(-1L),
            isAdmin = true,
            pageable = PageRequest.of(0, 5)
        )

        assertThat(result).hasSize(2)
    }

    private fun saveService(
        day: Int,
        title: String,
        employee: Employee,
        institution: Institution,
        assistancePlan: AssistancePlan
    ) {
        serviceRepository.save(
            Service(
                start = LocalDateTime.of(2026, 2, day, 9, 0),
                end = LocalDateTime.of(2026, 2, day, 10, 0),
                minutes = 60,
                title = title,
                content = "Inhalt",
                client = client,
                employee = employee,
                institution = institution,
                hourType = hourType,
                assistancePlan = assistancePlan
            )
        )
    }
}
