package de.vinz.openfls.domains.services.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.services.Service
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDateTime
import org.mockito.kotlin.whenever

@DataJpaTest
@Import(ServiceService::class, TestBeans::class)
class ServiceServiceDataJpaTest {

    @Autowired
    lateinit var serviceService: ServiceService

    @Autowired
    lateinit var serviceRepository: ServiceRepository

    @Autowired
    lateinit var clientRepository: ClientRepository

    @MockitoBean
    lateinit var clientService: ClientService

    @MockitoBean
    lateinit var assistancePlanService: AssistancePlanService

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
    fun create_archivedClient_throwsException() {
        // Given
        val archivedClient = Client(id = 11, archived = true)
        whenever(clientService.getById(11)).thenReturn(archivedClient)
        val entity = Service(
            start = LocalDateTime.of(2026, 2, 1, 9, 0),
            end = LocalDateTime.of(2026, 2, 1, 10, 0),
            client = archivedClient
        )

        // When / Then
        assertThatThrownBy { serviceService.create(entity) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("client is archived")
    }

    @Test
    fun create_archivedAssistancePlanClient_throwsException() {
        // Given
        val archivedClient = Client(id = 12, archived = true)
        val assistancePlan = AssistancePlan(id = 44, client = archivedClient)
        whenever(assistancePlanService.getById(44)).thenReturn(assistancePlan)
        val entity = Service(
            start = LocalDateTime.of(2026, 2, 1, 9, 0),
            end = LocalDateTime.of(2026, 2, 1, 10, 0),
            assistancePlan = assistancePlan
        )

        // When / Then
        assertThatThrownBy { serviceService.create(entity) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("client is archived")
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
    fun update_archivedClient_throwsException() {
        // Given
        val archivedClient = Client(id = 12, archived = true)
        whenever(clientService.getById(12)).thenReturn(archivedClient)
        val existing = serviceRepository.save(Service(start = LocalDateTime.of(2026, 2, 1, 8, 0), end = LocalDateTime.of(2026, 2, 1, 9, 0)))
        val updated = Service(
            id = existing.id,
            start = LocalDateTime.of(2026, 2, 1, 8, 0),
            end = LocalDateTime.of(2026, 2, 1, 10, 0),
            client = archivedClient
        )

        // When / Then
        assertThatThrownBy { serviceService.update(updated) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("client is archived")
    }

    @Test
    fun delete_archivedClient_throwsException() {
        // Given
        val archivedClient = clientRepository.save(Client(firstName = "Archived", lastName = "Client", archived = true))
        whenever(clientService.getById(archivedClient.id)).thenReturn(archivedClient)
        val existing = serviceRepository.save(
            Service(
                start = LocalDateTime.of(2026, 2, 1, 8, 0),
                end = LocalDateTime.of(2026, 2, 1, 9, 0),
                client = archivedClient
            )
        )

        // When / Then
        assertThatThrownBy { serviceService.delete(existing.id) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("client is archived")
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
