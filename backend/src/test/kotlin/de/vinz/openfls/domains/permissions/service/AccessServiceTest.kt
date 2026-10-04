package de.vinz.openfls.domains.permissions.service

import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.clients.service.ClientService
import de.vinz.openfls.domains.institutions.dto.InstitutionResponse
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.security.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AccessServiceTest {

    private val userService: UserService = mock()
    private val assistancePlanService: AssistancePlanService = mock()
    private val permissionService: PermissionService = mock()
    private val institutionService: InstitutionService = mock()
    private val clientService: ClientService = mock()
    private lateinit var accessService: AccessService

    @BeforeEach
    fun setUp() {
        accessService = AccessService(
            userService,
            assistancePlanService,
            permissionService,
            institutionService,
            clientService
        )
    }

    @Test
    fun getLeadingInstitutionIds_withLeadingPermissions_returnsInstitutionIds() {
        // Given
        whenever(userService.getUserId()).thenReturn(17L)
        whenever(permissionService.getLeadingInstitutionIdsByEmployee(17L)).thenReturn(listOf(31L))

        // When
        val result = accessService.getLeadingInstitutionIds()

        // Then
        assertThat(result).containsExactly(31L)
    }

    @Test
    fun getWriteRightsInstitutionIds_delegatesToPermissionService() {
        // Given
        whenever(permissionService.getWritableInstitutionIdsByEmployee(17L)).thenReturn(listOf(31L))

        // When
        val result = accessService.getWriteRightsInstitutionIds(17L)

        // Then
        assertThat(result).containsExactly(31L)
    }

    @Test
    fun getReadRightsInstitutionIds_admin_returnsAllInstitutions() {
        // Given
        whenever(userService.isAdmin()).thenReturn(true)
        whenever(institutionService.getAll()).thenReturn(
            listOf(InstitutionResponse(id = 1L), InstitutionResponse(id = 2L))
        )

        // When
        val result = accessService.getReadRightsInstitutionIds()

        // Then
        assertThat(result).containsExactly(1L, 2L)
    }

    @Test
    fun getReadRightsInstitutionIds_nonAdmin_delegatesToPermissionService() {
        // Given
        whenever(userService.isAdmin()).thenReturn(false)
        whenever(userService.getUserId()).thenReturn(17L)
        whenever(permissionService.getReadableInstitutionIdsByEmployee(17L)).thenReturn(listOf(31L))

        // When
        val result = accessService.getReadRightsInstitutionIds()

        // Then
        assertThat(result).containsExactly(31L)
    }

    @Test
    fun isLeader_leadingInstitution_returnsTrue() {
        // Given
        whenever(userService.isAdmin()).thenReturn(false)
        whenever(userService.getUserId()).thenReturn(17L)
        whenever(permissionService.getLeadingInstitutionIdsByEmployee(17L)).thenReturn(listOf(31L))

        // When / Then
        assertThat(accessService.isLeader(31L)).isTrue()
        assertThat(accessService.isLeader(32L)).isFalse()
    }

    @Test
    fun isAffiliated_admin_returnsTrueWithoutLookup() {
        // Given
        whenever(userService.isAdmin()).thenReturn(true)

        // When / Then
        assertThat(accessService.isAffiliated(31L)).isTrue()
    }

    @Test
    fun canReadEmployee_leaderOfAffiliatedInstitution_returnsTrue() {
        // Given
        whenever(userService.isAdmin()).thenReturn(false)
        whenever(userService.getUserId()).thenReturn(17L)
        whenever(permissionService.getLeadingInstitutionIdsByEmployee(17L)).thenReturn(listOf(31L))
        whenever(permissionService.getAffiliatedInstitutionIdsByEmployee(42L)).thenReturn(listOf(31L))

        // When
        val result = accessService.canReadEmployee(42L)

        // Then
        assertThat(result).isTrue()
    }
}
