package de.vinz.openfls.domains.employees

import de.vinz.openfls.domains.employees.dto.EmployeeCreateResult
import de.vinz.openfls.domains.employees.dto.EmployeeDeleteResult
import de.vinz.openfls.domains.employees.dto.EmployeeFavoriteResult
import de.vinz.openfls.domains.employees.dto.EmployeePasswordResetResult
import de.vinz.openfls.domains.employees.dto.EmployeeUpdateRoleResult
import de.vinz.openfls.domains.employees.dto.EmployeeUpdateResult
import de.vinz.openfls.domains.employees.dto.EmployeeDetailResponse
import de.vinz.openfls.domains.employees.service.EmployeeDeletionService
import de.vinz.openfls.domains.employees.service.EmployeeFavoriteService
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.common.web.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@WebMvcTest(EmployeeController::class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var employeeService: EmployeeService

    @MockitoBean
    lateinit var employeeDeletionService: EmployeeDeletionService

    @MockitoBean
    lateinit var employeeFavoriteService: EmployeeFavoriteService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @BeforeEach
    fun setUp() {
        given(accessService.getId()).willReturn(7L)
    }

    @Test
    fun getAll_adminWithIncludeArchived_returnsArchivedEmployees() {
        // Given
        val active = EmployeeDetailResponse(id = 1L, firstName = "Active", lastName = "Alpha")
        val archived = EmployeeDetailResponse(id = 2L, firstName = "Archived", lastName = "Zulu", archived = true)
        given(accessService.isAdmin()).willReturn(true)
        given(employeeService.getAllEmployeeDetails(false)).willReturn(listOf(active))
        given(employeeService.getAllEmployeeDetails(true)).willReturn(listOf(active, archived))

        // When
        val result = mockMvc.get("/employees?includeArchived=true").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":1")
        assertThat(result.response.contentAsString).contains("\"id\":2")
    }

    @Test
    fun getAll_nonAdminWithIncludeArchived_returnsOnlyActiveEmployees() {
        // Given
        val active = EmployeeDetailResponse(id = 1L, firstName = "Active", lastName = "Alpha")
        given(accessService.isAdmin()).willReturn(false)
        given(employeeService.getAllEmployeeDetails(false)).willReturn(listOf(active))

        // When
        val result = mockMvc.get("/employees?includeArchived=true").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).doesNotContain("\"id\":2")
        verify(employeeService, never()).getAllEmployeeDetails(true)
    }

    @Test
    fun getById_hiddenArchivedEmployee_returnsNotFound() {
        // Given
        given(accessService.isAdmin()).willReturn(false)
        given(employeeService.getEmployeeDetailById(7L, false)).willReturn(null)

        // When
        val result = mockMvc.get("/employees/7") {
            accept = MediaType.APPLICATION_JSON
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun create_validRequest_returnsEmployee() {
        // Given
        given(employeeService.create(any())).willReturn(
            EmployeeCreateResult.Success(EmployeeDetailResponse(id = 5L, firstName = "Max", lastName = "Muster"))
        )

        // When
        val result = postCreate()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":5")
    }

    @Test
    fun create_usernameTaken_returnsConflict() {
        // Given
        given(employeeService.create(any())).willReturn(EmployeeCreateResult.UsernameTaken)

        // When / Then
        assertThat(postCreate().response.status).isEqualTo(409)
    }

    @Test
    fun create_invalidUsername_returnsBadRequestWithReason() {
        // Given
        given(employeeService.create(any())).willReturn(EmployeeCreateResult.InvalidUsername("username is too short"))

        // When
        val result = postCreate()

        // Then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).isEqualTo("username is too short")
    }

    @Test
    fun updateRole_nonAdmin_returnsForbidden() {
        // Given
        given(accessService.isAdmin()).willReturn(false)

        // When
        val result = mockMvc.put("/employees/3/1").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(403)
        verify(employeeService, never()).updateRole(any(), any())
    }

    @Test
    fun updateRole_invalidRole_returnsBadRequest() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeService.updateRole(3L, 9)).willReturn(EmployeeUpdateRoleResult.InvalidRole)

        // When / Then
        assertThat(mockMvc.put("/employees/3/9").andReturn().response.status).isEqualTo(400)
    }

    @Test
    fun updateRole_unknownEmployee_returnsNotFound() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeService.updateRole(3L, 1)).willReturn(EmployeeUpdateRoleResult.NotFound)

        // When / Then
        assertThat(mockMvc.put("/employees/3/1").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun updateRole_admin_returnsEmployee() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeService.updateRole(3L, 2)).willReturn(
            EmployeeUpdateRoleResult.Success(EmployeeDetailResponse(id = 3L))
        )

        // When
        val result = mockMvc.put("/employees/3/2").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":3")
    }

    @Test
    fun resetPassword_nonAdmin_returnsForbidden() {
        // Given
        given(accessService.isAdmin()).willReturn(false)

        // When
        val result = mockMvc.put("/employees/reset_password/3").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(403)
        verify(employeeService, never()).resetPassword(any())
    }

    @Test
    fun resetPassword_unknownEmployee_returnsNotFound() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeService.resetPassword(3L)).willReturn(EmployeePasswordResetResult.NotFound)

        // When / Then
        assertThat(mockMvc.put("/employees/reset_password/3").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun update_differentPathAndRequestId_returnsBadRequest() {
        // Given
        given(accessService.canModifyEmployee()).willReturn(true)

        // When
        val result = mockMvc.put("/employees/3") {
            contentType = MediaType.APPLICATION_JSON
            content = updateJson(id = 4)
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun update_withoutPermission_returnsForbidden() {
        // Given
        given(accessService.canModifyEmployee()).willReturn(false)

        // When
        val result = mockMvc.put("/employees/3") {
            contentType = MediaType.APPLICATION_JSON
            content = updateJson(id = 3)
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(403)
    }

    @Test
    fun update_unknownEmployee_returnsNotFound() {
        // Given
        given(accessService.canModifyEmployee()).willReturn(true)
        given(employeeService.update(any(), any())).willReturn(EmployeeUpdateResult.NotFound)

        // When
        val result = mockMvc.put("/employees/3") {
            contentType = MediaType.APPLICATION_JSON
            content = updateJson(id = 3)
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun delete_nonAdmin_returnsForbidden() {
        // Given
        given(accessService.isAdmin()).willReturn(false)

        // When
        val result = mockMvc.delete("/employees/3").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(403)
        verify(employeeDeletionService, never()).delete(any())
    }

    @Test
    fun delete_employeeWithServices_returnsConflict() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeDeletionService.delete(3L)).willReturn(EmployeeDeleteResult.HasServices)

        // When / Then
        assertThat(mockMvc.delete("/employees/3").andReturn().response.status).isEqualTo(409)
    }

    @Test
    fun delete_unknownEmployee_returnsNotFound() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeDeletionService.delete(3L)).willReturn(EmployeeDeleteResult.NotFound)

        // When / Then
        assertThat(mockMvc.delete("/employees/3").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun delete_admin_returnsDeletedEmployee() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeDeletionService.delete(3L)).willReturn(
            EmployeeDeleteResult.Success(EmployeeDetailResponse(id = 3L))
        )

        // When
        val result = mockMvc.delete("/employees/3").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":3")
    }

    @Test
    fun addAssistancePlanFavorite_storesTheFavoriteForTheSignedInEmployee() {
        // Given
        given(employeeFavoriteService.addAssistancePlanFavorite(7L, 12L)).willReturn(EmployeeFavoriteResult.Success)

        // When
        val result = mockMvc.post("/employees/assistance_plan/favorite/12").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        verify(employeeFavoriteService).addAssistancePlanFavorite(7L, 12L)
    }

    @Test
    fun addAssistancePlanFavorite_unknownAssistancePlan_returnsBadRequest() {
        // Given
        given(employeeFavoriteService.addAssistancePlanFavorite(7L, 12L))
            .willReturn(EmployeeFavoriteResult.AssistancePlanNotFound)

        // When / Then
        assertThat(mockMvc.post("/employees/assistance_plan/favorite/12").andReturn().response.status).isEqualTo(400)
    }

    private fun postCreate() = mockMvc.post("/employees") {
        contentType = MediaType.APPLICATION_JSON
        content = """
            {
              "firstName": "Max",
              "lastName": "Muster",
              "access": {"username": "maxmuster", "role": 3}
            }
        """.trimIndent()
    }.andReturn()

    private fun updateJson(id: Long) = """{"id": $id, "firstName": "Max", "lastName": "Muster"}"""
}
