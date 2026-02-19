package de.vinz.openfls.domains.hourTypes

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(HourTypeService::class)
class HourTypeServiceDataJpaTest {

    @Autowired
    lateinit var hourTypeService: HourTypeService

    @Autowired
    lateinit var hourTypeRepository: HourTypeRepository

    @Test
    fun create_validDto_persistsEntity() {
        // Given
        val dto = HourTypeDto(title = "Standard", price = 42.5)

        // When
        val result = hourTypeService.create(dto)

        // Then
        val saved = hourTypeRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().title).isEqualTo("Standard")
    }

    @Test
    fun update_existingDto_updatesEntity() {
        // Given
        val existing = hourTypeRepository.save(HourType(title = "Old", price = 1.0))
        val dto = HourTypeDto(id = existing.id, title = "New", price = 2.5)

        // When
        val result = hourTypeService.update(dto)

        // Then
        val saved = hourTypeRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().title).isEqualTo("New")
        assertThat(saved.get().price).isEqualTo(2.5)
    }
}
