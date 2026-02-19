package de.vinz.openfls.domains.services

import com.fasterxml.jackson.databind.ObjectMapper
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.contingents.services.ContingentCalendarService
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.permissions.PermissionService
import de.vinz.openfls.domains.services.dtos.ServiceDto
import de.vinz.openfls.domains.services.projections.FromTillEmployeeServiceProjection
import de.vinz.openfls.domains.services.services.ServiceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(ServiceController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class ServiceControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var serviceService: ServiceService

    @MockitoBean
    lateinit var contingentCalendarService: ContingentCalendarService

    @MockitoBean
    lateinit var employeeService: EmployeeService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var permissionService: PermissionService

    @MockitoBean
    lateinit var assistancePlanService: AssistancePlanService

    @Test
    fun getByClientAndDate_validRequest_returnsResponse() {
        // Given
        val date = LocalDate.of(2026, 2, 8)
        val projection = object : FromTillEmployeeServiceProjection {
            override val id: Long = 1
            override val start: LocalDateTime = LocalDateTime.of(2026, 2, 8, 8, 0)
            override val end: LocalDateTime = LocalDateTime.of(2026, 2, 8, 9, 0)
            override val employeeFirstname: String = "Max"
            override val employeeLastname: String = "Mustermann"
        }
        given(serviceService.getFromTillEmployeeNameProjectionByClientAndDate(1L, date))
            .willReturn(listOf(projection))

        val payload = mapOf("clientId" to 1, "date" to date.toString())

        // When
        val result = mockMvc.post("/services/client-and-date") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andReturn()

        // Then
        val json = result.response.contentAsString
        assertThat(result.response.status).isEqualTo(200)
        assertThat(json).contains("\"clientId\":1")
        assertThat(json).contains("\"timepoint\":\"08:00 - 09:00\"")
        assertThat(json).contains("\"employeeName\":\"M. Mustermann\"")
    }

    @Test
    fun getByClientAndDate_serviceThrows_returnsBadRequest() {
        // Given
        val date = LocalDate.of(2026, 2, 8)
        doThrow(IllegalArgumentException("boom"))
            .`when`(serviceService)
            .getFromTillEmployeeNameProjectionByClientAndDate(1L, date)

        val payload = mapOf("clientId" to 1, "date" to date.toString())

        // When
        val result = mockMvc.post("/services/client-and-date") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun delete_ownService_nonAdmin_returnsOk() {
        // Given
        val serviceId = 11L
        val employeeId = 5L
        val service = Service(
            id = serviceId,
            start = LocalDateTime.of(2026, 2, 8, 8, 0),
            end = LocalDateTime.of(2026, 2, 8, 9, 0),
            employee = Employee(id = employeeId)
        )

        given(serviceService.existsById(serviceId)).willReturn(true)
        given(serviceService.getById(serviceId)).willReturn(service)
        given(serviceService.getDtoById(serviceId)).willReturn(ServiceDto().apply { id = serviceId })
        given(accessService.isAdmin()).willReturn(false)
        given(accessService.getId()).willReturn(employeeId)

        // When
        val result = mockMvc.delete("/services/$serviceId").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
    }

    @Test
    fun delete_foreignService_nonAdmin_returnsBadRequest() {
        // Given
        val serviceId = 12L
        val service = Service(
            id = serviceId,
            start = LocalDateTime.of(2026, 2, 8, 8, 0),
            end = LocalDateTime.of(2026, 2, 8, 9, 0),
            employee = Employee(id = 9L)
        )

        given(serviceService.existsById(serviceId)).willReturn(true)
        given(serviceService.getById(serviceId)).willReturn(service)
        given(accessService.isAdmin()).willReturn(false)
        given(accessService.getId()).willReturn(5L)

        // When
        val result = mockMvc.delete("/services/$serviceId").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains("No permission to delete this service")
    }
}
