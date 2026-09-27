package de.vinz.openfls.domains.sponsors

import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.Unprofessional
import de.vinz.openfls.domains.employees.entities.UnprofessionalKey
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.UnprofessionalRepository
import de.vinz.openfls.domains.sponsors.dtos.SponsorCreateRequest
import de.vinz.openfls.domains.sponsors.dtos.SponsorUpdateRequest
import de.vinz.openfls.domains.sponsors.dtos.SponsorUpdateResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.LocalDate

@DataJpaTest
@Import(SponsorService::class)
class SponsorServiceDataJpaTest {

    @Autowired
    lateinit var sponsorService: SponsorService

    @Autowired
    lateinit var sponsorRepository: SponsorRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var unprofessionalRepository: UnprofessionalRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun create_validRequest_persistsEntity() {
        // Given
        val request = SponsorCreateRequest(name = "Sponsor A", payOverhang = true, payExact = false)

        // When
        val result = sponsorService.create(request)

        // Then
        val saved = sponsorRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().name).isEqualTo("Sponsor A")
        assertThat(result.payOverhang).isTrue()
    }

    @Test
    fun update_existingSponsor_updatesEntity() {
        // Given
        val existing = sponsorRepository.save(Sponsor(name = "Old", payOverhang = false, payExact = false))
        val request = SponsorUpdateRequest(id = existing.id, name = "New", payOverhang = true, payExact = true)

        // When
        val result = sponsorService.update(request) as SponsorUpdateResult.Success

        // Then
        val saved = sponsorRepository.findById(result.response.id)
        assertThat(saved).isPresent
        assertThat(saved.get().name).isEqualTo("New")
        assertThat(saved.get().payExact).isTrue()
    }

    @Test
    fun update_missingSponsor_returnsNotFound() {
        // When
        val result = sponsorService.update(SponsorUpdateRequest(id = 9999, name = "New"))

        // Then
        assertThat(result).isEqualTo(SponsorUpdateResult.NotFound)
    }

    @Test
    fun getAll_returnsSponsorsSortedByNameIgnoringCase() {
        // Given
        sponsorRepository.save(Sponsor(name = "beta"))
        sponsorRepository.save(Sponsor(name = "Alpha"))

        // When
        val result = sponsorService.getAll()

        // Then
        assertThat(result.map { it.name }).containsExactly("Alpha", "beta")
    }

    @Test
    fun getById_existingSponsor_includesUnprofessionals() {
        // Given
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor"))
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Muster"))
        val end = LocalDate.of(2026, 5, 1)
        unprofessionalRepository.save(
            Unprofessional(
                id = UnprofessionalKey(employeeId = employee.id, sponsorId = sponsor.id),
                employee = employee,
                sponsor = sponsor,
                end = end
            )
        )
        entityManager.flush()
        entityManager.clear()

        // When
        val result = sponsorService.getById(sponsor.id)

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.name).isEqualTo("Sponsor")
        assertThat(result.unprofessionals).hasSize(1)
        assertThat(result.unprofessionals.single().employeeId).isEqualTo(employee.id)
        assertThat(result.unprofessionals.single().end).isEqualTo(end)
    }

    @Test
    fun getById_missingSponsor_returnsNull() {
        assertThat(sponsorService.getById(9999)).isNull()
    }

    @Test
    fun delete_existingSponsor_removesEntity() {
        // Given
        val sponsor = sponsorRepository.save(Sponsor(name = "Delete"))

        // When
        sponsorService.delete(sponsor.id)

        // Then
        assertThat(sponsorRepository.existsById(sponsor.id)).isFalse()
    }

    @Test
    fun getEntityById_existingSponsor_returnsEntity() {
        // Given
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor"))

        // When / Then
        assertThat(sponsorService.getEntityById(sponsor.id)).isEqualTo(sponsor)
    }
}
