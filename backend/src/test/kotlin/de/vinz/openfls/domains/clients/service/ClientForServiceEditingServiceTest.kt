package de.vinz.openfls.domains.clients.service

import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanForServiceEditingResponse
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.permissions.service.AccessService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class ClientForServiceEditingServiceTest {

    private val clientService: ClientService = mock()
    private val assistancePlanService: AssistancePlanService = mock()
    private val accessService: AccessService = mock()
    private val service = ClientForServiceEditingService(clientService, assistancePlanService, accessService)

    @BeforeEach
    fun setUp() {
        whenever(accessService.getId()).thenReturn(8L)
        whenever(accessService.getWriteRightsInstitutionIds(8L)).thenReturn(listOf(5L, 6L))
    }

    @Test
    fun getForServiceEditingById_returnsClientWithPlansOfWritableInstitutions() {
        // Given
        whenever(clientService.getEntityById(3L)).thenReturn(client(archived = false))
        whenever(accessService.isAdmin()).thenReturn(false)
        whenever(accessService.getLeadingInstitutionIds()).thenReturn(emptyList())
        whenever(assistancePlanService.getAllForServiceEditingByClientId(3L, listOf(5L, 6L)))
            .thenReturn(listOf(planResponse(11L)))

        // When
        val result = service.getForServiceEditingById(3L)

        // Then
        assertThat(result!!.id).isEqualTo(3L)
        assertThat(result.firstName).isEqualTo("Max")
        assertThat(result.institution.id).isEqualTo(5L)
        assertThat(result.assistancePlans.map { it.id }).containsExactly(11L)
    }

    @Test
    fun getForServiceEditingById_unknownClient_returnsNull() {
        whenever(clientService.getEntityById(3L)).thenReturn(null)

        assertThat(service.getForServiceEditingById(3L)).isNull()
        verify(assistancePlanService, never()).getAllForServiceEditingByClientId(any(), any())
    }

    @Test
    fun getForServiceEditingById_archivedClient_isHiddenUnlessAdminOrLeader() {
        // Given
        whenever(clientService.getEntityById(3L)).thenReturn(client(archived = true))
        whenever(assistancePlanService.getAllForServiceEditingByClientId(any(), any())).thenReturn(emptyList())
        whenever(accessService.isAdmin()).thenReturn(false, false, true)
        whenever(accessService.getLeadingInstitutionIds()).thenReturn(emptyList(), listOf(5L))

        // When / Then
        assertThat(service.getForServiceEditingById(3L)).isNull()
        assertThat(service.getForServiceEditingById(3L)).isNotNull
        assertThat(service.getForServiceEditingById(3L)).isNotNull
    }

    private fun client(archived: Boolean) = Client(
        id = 3L,
        firstName = "Max",
        lastName = "Mustermann",
        archived = archived,
        institution = Institution(id = 5L, name = "Inst")
    )

    private fun planResponse(id: Long) = AssistancePlanForServiceEditingResponse(
        id = id,
        start = LocalDate.of(2026, 1, 1),
        end = LocalDate.of(2026, 12, 31),
        clientId = 3L,
        institutionId = 5L,
        institutionName = "Inst",
        sponsorId = 1L,
        hourMode = AssistancePlanHourMode.EXACT,
        hourCorridorId = 0,
        goals = emptyList(),
        hours = emptyList(),
        possibleDocumentationHourTypes = emptyList()
    )
}
