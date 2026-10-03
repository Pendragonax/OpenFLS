package de.vinz.openfls.domains.employees.service

import de.vinz.openfls.domains.employees.repository.EmployeeRepository
import de.vinz.openfls.domains.employees.repository.UnprofessionalRepository
import de.vinz.openfls.domains.employees.dto.UnprofessionalRequest
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.entity.Unprofessional
import de.vinz.openfls.domains.employees.entity.UnprofessionalKey
import de.vinz.openfls.domains.sponsors.entity.Sponsor
import de.vinz.openfls.domains.sponsors.repository.SponsorRepository
import de.vinz.openfls.domains.sponsors.service.SponsorService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDate

@DataJpaTest
@Import(UnprofessionalService::class, SponsorService::class, TestBeans::class)
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
        unprofessionalService.createEntity(entity)

        // Then
        val saved = unprofessionalRepository.findByEmployeeId(employee.id!!)
        assertThat(saved).hasSize(1)
        assertThat(saved.first().end).isEqualTo(LocalDate.of(2026, 2, 1))
    }

    @Test
    fun convertToUnprofessionals_knownSponsors_buildsEntitiesForTheEmployee() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor"))
        val requests = listOf(UnprofessionalRequest(sponsorId = sponsor.id, end = LocalDate.of(2026, 2, 1)))

        // When
        val result = unprofessionalService.buildEntitiesFromRequests(requests, employee)

        // Then
        val unprofessional = result!!.single()
        assertThat(unprofessional.id).isEqualTo(UnprofessionalKey(employeeId = employee.id, sponsorId = sponsor.id))
        assertThat(unprofessional.employee).isEqualTo(employee)
        assertThat(unprofessional.sponsor).isEqualTo(sponsor)
        assertThat(unprofessional.end).isEqualTo(LocalDate.of(2026, 2, 1))
    }

    @Test
    fun convertToUnprofessionals_unknownSponsor_returnsNull() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))

        // When
        val result = unprofessionalService.buildEntitiesFromRequests(listOf(UnprofessionalRequest(sponsorId = 9999)), employee)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun deleteByEmployeeIdAndSponsorId_removesOnlyThatEntry() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val first = sponsorRepository.save(Sponsor(name = "First"))
        val second = sponsorRepository.save(Sponsor(name = "Second"))
        listOf(first, second).forEach {
            unprofessionalService.createEntity(
                Unprofessional(id = UnprofessionalKey(employee.id, it.id), employee = employee, sponsor = it)
            )
        }

        // When
        unprofessionalService.deleteByEmployeeIdAndSponsorId(employee.id!!, first.id)

        // Then
        val remaining = unprofessionalService.getAllEntitiesByEmployeeId(employee.id!!)
        assertThat(remaining.map { it.id?.sponsorId }).containsExactly(second.id)
    }
}
