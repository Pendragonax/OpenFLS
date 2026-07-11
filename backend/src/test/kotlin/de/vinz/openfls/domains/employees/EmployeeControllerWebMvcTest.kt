package de.vinz.openfls.domains.employees

import de.vinz.openfls.domains.employees.dtos.EmployeeDto
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(EmployeeController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var employeeService: EmployeeService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var userService: UserService

    @Test
    fun getAll_adminWithIncludeArchived_returnsArchivedEmployees() {
        // Given
        val active = EmployeeDto().apply {
            id = 1L
            firstName = "Active"
            lastName = "Alpha"
        }
        val archived = EmployeeDto().apply {
            id = 2L
            firstName = "Archived"
            lastName = "Zulu"
            this.archived = true
        }
        given(accessService.isAdmin()).willReturn(true)
        given(employeeService.getAllEmployeeDtos()).willReturn(listOf(active))
        given(employeeService.getAllEmployeeDtos(includeArchived = true)).willReturn(listOf(active, archived))

        // When
        val result = mockMvc.get("/employees?includeArchived=true").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":1")
        assertThat(result.response.contentAsString).contains("\"id\":2")
    }

    @Test
    fun getById_hiddenArchivedEmployee_returnsBadRequest() {
        // Given
        given(accessService.isAdmin()).willReturn(false)
        given(employeeService.getEmployeeDtoById(7L, false)).willReturn(null)

        // When
        val result = mockMvc.get("/employees/7") {
            accept = MediaType.APPLICATION_JSON
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }
}
