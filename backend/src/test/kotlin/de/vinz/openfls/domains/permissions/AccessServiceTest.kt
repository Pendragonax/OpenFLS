package de.vinz.openfls.domains.permissions

import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.employees.entities.EmployeeInstitutionRightsKey
import de.vinz.openfls.domains.goals.services.GoalService
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.services.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AccessServiceTest {

    private val userService: UserService = mock()
    private val goalService: GoalService = mock()
    private val assistancePlanService: AssistancePlanService = mock()
    private val permissionService: PermissionService = mock()
    private val institutionService: InstitutionService = mock()
    private val clientService: ClientService = mock()
    private val accessService = AccessService(
        userService,
        goalService,
        assistancePlanService,
        permissionService,
        institutionService,
        clientService
    )

    @Test
    fun getLeadingInstitutionIds_withLeadingPermissions_returnsInstitutionIds() {
        // Given
        whenever(userService.getUserId()).thenReturn(17L)
        whenever(permissionService.getPermissionByEmployee(17L)).thenReturn(
            listOf(
                Permission(
                    id = EmployeeInstitutionRightsKey(employeeId = 17L, institutionId = 31L),
                    institution = Institution(id = 31L, name = "Lead institution"),
                    changeInstitution = true
                ),
                Permission(
                    id = EmployeeInstitutionRightsKey(employeeId = 17L, institutionId = 32L),
                    institution = Institution(id = 32L, name = "Other institution"),
                    changeInstitution = false
                )
            )
        )

        // When
        val result = accessService.getLeadingInstitutionIds()

        // Then
        assertThat(result).containsExactly(31L)
    }
}
