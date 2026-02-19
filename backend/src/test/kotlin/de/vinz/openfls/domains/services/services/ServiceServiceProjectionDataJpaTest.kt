package de.vinz.openfls.domains.services.services

import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.domains.services.Service
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest
@Import(ServiceService::class, TestBeans::class)
class ServiceServiceProjectionDataJpaTest {

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

    @Test
    fun getFromTillEmployeeNameProjectionByClientAndDate_matchingDate_returnsProjection() {
        // Given
        val client = clientRepository.save(Client(firstName = "Anna", lastName = "Muster"))
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val date = LocalDate.of(2026, 2, 8)
        serviceRepository.save(
            Service(
                start = LocalDateTime.of(2026, 2, 8, 8, 0),
                end = LocalDateTime.of(2026, 2, 8, 9, 0),
                client = client,
                employee = employee
            )
        )
        serviceRepository.save(
            Service(
                start = LocalDateTime.of(2026, 2, 9, 8, 0),
                end = LocalDateTime.of(2026, 2, 9, 9, 0),
                client = client,
                employee = employee
            )
        )

        // When
        val result = serviceService.getFromTillEmployeeNameProjectionByClientAndDate(client.id, date)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result.first().employeeFirstname).isEqualTo("Max")
        assertThat(result.first().employeeLastname).isEqualTo("Mustermann")
        assertThat(result.first().start.toLocalDate()).isEqualTo(date)
    }

    @Test
    fun getFromTillEmployeeNameProjectionByClientAndDate_noMatchingDate_returnsEmptyList() {
        // Given
        val client = clientRepository.save(Client(firstName = "Anna", lastName = "Muster"))
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        serviceRepository.save(
            Service(
                start = LocalDateTime.of(2026, 2, 8, 8, 0),
                end = LocalDateTime.of(2026, 2, 8, 9, 0),
                client = client,
                employee = employee
            )
        )

        // When
        val result = serviceService.getFromTillEmployeeNameProjectionByClientAndDate(
            client.id,
            LocalDate.of(2026, 2, 10)
        )

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun getByEmployeeAndStartAndEnd_returnsProjectionWithEnd() {
        // Given
        val client = clientRepository.save(Client(firstName = "Anna", lastName = "Muster"))
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val institution = institutionRepository.save(Institution(name = "Inst", email = "inst@test.de", phonenumber = "123"))
        val start = LocalDateTime.of(2026, 2, 8, 8, 0)
        val end = LocalDateTime.of(2026, 2, 8, 9, 30)
        serviceRepository.save(
            Service(
                start = start,
                end = end,
                client = client,
                employee = employee,
                institution = institution
            )
        )

        // When
        val result = serviceService.getByEmployeeAndStartAndEnd(
            employee.id!!,
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 28)
        )

        // Then
        assertThat(result).hasSize(1)
        assertThat(result.first().start).isEqualTo(start)
        assertThat(result.first().end).isEqualTo(end)
    }
}
