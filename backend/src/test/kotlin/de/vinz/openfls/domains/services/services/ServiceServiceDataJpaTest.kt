package de.vinz.openfls.domains.services.services

import de.vinz.openfls.domains.services.Service
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

@DataJpaTest
@Import(ServiceService::class, TestBeans::class)
class ServiceServiceDataJpaTest {

    @Autowired
    lateinit var serviceService: ServiceService

    @Autowired
    lateinit var serviceRepository: ServiceRepository

    @Test
    fun create_validEntity_persistsAndCalculatesMinutes() {
        // Given
        val start = LocalDateTime.of(2026, 2, 1, 9, 0)
        val end = LocalDateTime.of(2026, 2, 1, 10, 30)
        val entity = Service(start = start, end = end, title = "A")

        // When
        val result = serviceService.create(entity)

        // Then
        val saved = serviceRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().minutes).isEqualTo(90)
    }

    @Test
    fun create_idSet_throwsException() {
        // Given
        val entity = Service(id = 1, start = LocalDateTime.now(), end = LocalDateTime.now().plusHours(1))

        // When / Then
        assertThatThrownBy { serviceService.create(entity) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun create_endBeforeStart_throwsException() {
        // Given
        val start = LocalDateTime.of(2026, 2, 1, 10, 0)
        val end = LocalDateTime.of(2026, 2, 1, 9, 0)
        val entity = Service(start = start, end = end)

        // When / Then
        assertThatThrownBy { serviceService.create(entity) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_idMissing_throwsException() {
        // Given
        val entity = Service(id = 0, start = LocalDateTime.now(), end = LocalDateTime.now().plusHours(1))

        // When / Then
        assertThatThrownBy { serviceService.update(entity) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_idNotFound_throwsException() {
        // Given
        val entity = Service(id = 9999, start = LocalDateTime.now(), end = LocalDateTime.now().plusHours(1))

        // When / Then
        assertThatThrownBy { serviceService.update(entity) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_endBeforeStart_throwsException() {
        // Given
        val existing = serviceRepository.save(Service(start = LocalDateTime.of(2026, 2, 1, 8, 0), end = LocalDateTime.of(2026, 2, 1, 9, 0)))
        val updated = Service(id = existing.id, start = LocalDateTime.of(2026, 2, 1, 10, 0), end = LocalDateTime.of(2026, 2, 1, 9, 0))

        // When / Then
        assertThatThrownBy { serviceService.update(updated) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_validEntity_updatesMinutes() {
        // Given
        val existing = serviceRepository.save(Service(start = LocalDateTime.of(2026, 2, 1, 8, 0), end = LocalDateTime.of(2026, 2, 1, 9, 0)))
        val updated = Service(id = existing.id, start = LocalDateTime.of(2026, 2, 1, 8, 0), end = LocalDateTime.of(2026, 2, 1, 10, 0))

        // When
        val result = serviceService.update(updated)

        // Then
        val saved = serviceRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().minutes).isEqualTo(120)
    }
}
