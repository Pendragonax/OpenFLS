package de.vinz.openfls.domains.hourTypes

import de.vinz.openfls.domains.hourTypes.dtos.HourTypeCreateRequest
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
    fun create_validRequest_persistsEntity() {
        // Given
        val request = HourTypeCreateRequest(title = "Standard", price = 42.5)

        // When
        val result = hourTypeService.create(request)

        // Then
        val saved = hourTypeRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().title).isEqualTo("Standard")
        assertThat(result.price).isEqualTo(42.5)
    }

    @Test
    fun update_existingRequest_updatesEntity() {
        // Given
        val existing = hourTypeRepository.save(HourType(title = "Old", price = 1.0))
        val request = HourTypeUpdateRequest(id = existing.id, title = "New", price = 2.5)

        // When
        val result = hourTypeService.update(request)

        // Then
        val saved = hourTypeRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().title).isEqualTo("New")
        assertThat(saved.get().price).isEqualTo(2.5)
    }

    @Test
    fun update_missingHourType_throwsException() {
        // Given
        val request = HourTypeUpdateRequest(id = 9999, title = "New", price = 2.5)

        // When / Then
        assertThatThrownBy { hourTypeService.update(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun delete_existingHourType_removesEntity() {
        // Given
        val existing = hourTypeRepository.save(HourType(title = "Delete", price = 1.0))

        // When
        hourTypeService.delete(existing.id)

        // Then
        assertThat(hourTypeRepository.existsById(existing.id)).isFalse()
    }

    @Test
    fun getAll_returnsResponsesSortedByTitleIgnoringCase() {
        // Given
        hourTypeRepository.save(HourType(title = "beta", price = 1.0))
        hourTypeRepository.save(HourType(title = "Alpha", price = 2.0))

        // When
        val result = hourTypeService.getAll()

        // Then
        assertThat(result.map { it.title }).containsExactly("Alpha", "beta")
    }

    @Test
    fun getById_existingAndMissing_returnsResponseOrNull() {
        // Given
        val existing = hourTypeRepository.save(HourType(title = "Standard", price = 3.0))

        // When / Then
        assertThat(hourTypeService.getById(existing.id)?.title).isEqualTo("Standard")
        assertThat(hourTypeService.getById(9999)).isNull()
    }

    @Test
    fun getEntityById_existing_returnsEntity() {
        // Given
        val existing = hourTypeRepository.save(HourType(title = "Standard", price = 3.0))

        // When
        val result = hourTypeService.getEntityById(existing.id)

        // Then
        assertThat(result).isEqualTo(existing)
    }
}
