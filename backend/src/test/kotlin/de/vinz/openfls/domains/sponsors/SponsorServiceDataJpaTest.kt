package de.vinz.openfls.domains.sponsors

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(SponsorService::class)
class SponsorServiceDataJpaTest {

    @Autowired
    lateinit var sponsorService: SponsorService

    @Autowired
    lateinit var sponsorRepository: SponsorRepository

    @Test
    fun create_validSponsor_persistsEntity() {
        // Given
        val dto = SponsorDto(name = "Sponsor A", payOverhang = true, payExact = false)

        // When
        val result = sponsorService.create(dto)

        // Then
        val saved = sponsorRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().name).isEqualTo("Sponsor A")
    }

    @Test
    fun update_existingSponsor_updatesEntity() {
        // Given
        val existing = sponsorRepository.save(Sponsor(name = "Old", payOverhang = false, payExact = false))
        val dto = SponsorDto(id = existing.id, name = "New", payOverhang = true, payExact = true)

        // When
        val result = sponsorService.update(dto)

        // Then
        val saved = sponsorRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().name).isEqualTo("New")
        assertThat(saved.get().payExact).isTrue()
    }
}
