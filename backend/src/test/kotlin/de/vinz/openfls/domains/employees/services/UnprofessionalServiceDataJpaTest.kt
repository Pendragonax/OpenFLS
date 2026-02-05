package de.vinz.openfls.domains.employees.services

import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.UnprofessionalRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.Unprofessional
import de.vinz.openfls.domains.employees.entities.UnprofessionalKey
import de.vinz.openfls.domains.sponsors.Sponsor
import de.vinz.openfls.domains.sponsors.SponsorRepository
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDate

@DataJpaTest
@Import(UnprofessionalService::class, TestBeans::class)
class UnprofessionalServiceDataJpaTest {

    @Autowired
    lateinit var unprofessionalService: UnprofessionalService

    @Autowired
    lateinit var unprofessionalRepository: UnprofessionalRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var sponsorRepository: SponsorRepository

    @Test
    fun create_validEntity_persistsEntry() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))
        val entity = Unprofessional(
            id = UnprofessionalKey(employeeId = employee.id, sponsorId = sponsor.id),
            employee = employee,
            sponsor = sponsor,
            end = LocalDate.of(2026, 2, 1)
        )

        // When
        val result = unprofessionalService.create(entity)

        // Then
        val saved = unprofessionalRepository.findByEmployeeId(employee.id!!)
        assertThat(saved).hasSize(1)
        assertThat(saved.first().end).isEqualTo(LocalDate.of(2026, 2, 1))
    }

    @Test
    fun update_existingEntity_updatesEndDate() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))
        val existing = unprofessionalRepository.save(Unprofessional(
            id = UnprofessionalKey(employeeId = employee.id, sponsorId = sponsor.id),
            employee = employee,
            sponsor = sponsor,
            end = LocalDate.of(2026, 1, 1)
        ))
        val updated = Unprofessional(
            id = existing.id,
            employee = employee,
            sponsor = sponsor,
            end = LocalDate.of(2026, 3, 1)
        )

        // When
        val result = unprofessionalService.update(updated)

        // Then
        val saved = unprofessionalRepository.findByEmployeeId(employee.id!!)
        assertThat(saved).hasSize(1)
        assertThat(saved.first().end).isEqualTo(LocalDate.of(2026, 3, 1))
    }
}
