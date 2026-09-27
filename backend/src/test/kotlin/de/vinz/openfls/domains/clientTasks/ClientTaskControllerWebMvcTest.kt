package de.vinz.openfls.domains.clientTasks

import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskDto
import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskPageDto
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.clients.dtos.ClientDto
import de.vinz.openfls.domains.employees.dtos.EmployeeWithAccess
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(ClientTaskController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class ClientTaskControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var clientTaskService: ClientTaskService

    @MockitoBean
    lateinit var clientService: ClientService

    @MockitoBean
    lateinit var employeeService: EmployeeService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @BeforeEach
    fun setUp() {
        given(accessService.getId()).willReturn(7L)
        given(employeeService.getEmployeeDtoById(eq(7L), any())).willReturn(EmployeeWithAccess().apply {
            id = 7
            firstName = "Anna"
            lastName = "Autorin"
        })
    }

    @Test
    fun create_everyEmployeeMayCreateATask() {
        given(clientService.existsById(3L)).willReturn(true)
        given(accessService.isAdmin()).willReturn(false)
        given(clientTaskService.create(any(), eq(7L), eq("Anna Autorin"))).willReturn(taskDto())

        val result = mockMvc.post("/client_tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"clientId":3,"title":"Bericht","description":"","dueDate":"2026-03-20"}"""
        }.andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"title\":\"Bericht\"")
    }

    @Test
    fun create_unknownClient_returnsBadRequest() {
        given(clientService.existsById(3L)).willReturn(false)

        val result = mockMvc.post("/client_tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"clientId":3,"title":"Bericht","description":"","dueDate":"2026-03-20"}"""
        }.andReturn()

        assertThat(result.response.status).isEqualTo(400)
        verify(clientTaskService, never()).create(any(), any(), any())
    }

    @Test
    fun getByClientId_everyEmployeeMaySeeTheTasks() {
        given(clientService.existsById(3L)).willReturn(true)
        given(accessService.isAdmin()).willReturn(false)
        given(clientTaskService.getDtosByClientId(3L)).willReturn(listOf(taskDto()))

        val result = mockMvc.get("/client_tasks/client/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"title\":\"Bericht\"")
    }

    @Test
    fun complete_everyEmployeeMayTickOffATask() {
        given(clientTaskService.complete(eq(1L), any(), eq(7L), eq("Anna Autorin")))
            .willReturn(taskDto().copy(done = true, completionComment = "erledigt"))

        val result = mockMvc.post("/client_tasks/1/complete") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"comment":"erledigt","completedOn":"2026-03-10"}"""
        }.andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"done\":true")
    }

    @Test
    fun change_usesPathIdAndDedicatedActionEndpoint() {
        given(clientTaskService.update(eq(1L), any(), eq(7L), eq("Anna Autorin"))).willReturn(taskDto())

        val result = mockMvc.put("/client_tasks/1/change") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"Bericht","description":"Neu","dueDate":"2026-03-20"}"""
        }.andReturn()

        assertThat(result.response.status).isEqualTo(200)
        verify(clientTaskService).update(eq(1L), any(), eq(7L), eq("Anna Autorin"))
    }

    @Test
    fun completed_returnsRequestedPage() {
        given(clientService.existsById(3L)).willReturn(true)
        given(clientTaskService.getCompletedDtosByClientId(3L, 1, 10))
            .willReturn(ClientTaskPageDto(listOf(taskDto().copy(done = true)), 1, 10, 11, 2))

        val result = mockMvc.get("/client_tasks/client/3/completed?page=1&size=10").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"totalElements\":11")
    }

    @Test
    fun delete_removesTheTask() {
        given(clientTaskService.getDtoById(1L)).willReturn(taskDto())

        val result = mockMvc.delete("/client_tasks/1").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        verify(clientTaskService).delete(eq(1L), eq(7L), eq("Anna Autorin"))
    }

    @Test
    fun getHistory_unknownTask_returnsBadRequest() {
        given(clientTaskService.existsById(1L)).willReturn(false)

        val result = mockMvc.get("/client_tasks/1/history").andReturn()

        assertThat(result.response.status).isEqualTo(400)
        verify(clientTaskService, never()).getAuditHistory(any())
    }

    @Test
    fun getHistory_existingTask_returnsTheAuditTrail() {
        given(clientTaskService.existsById(1L)).willReturn(true)
        given(clientTaskService.getAuditHistory(1L)).willReturn(emptyList())

        val result = mockMvc.get("/client_tasks/1/history").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        verify(clientTaskService).getAuditHistory(1L)
    }

    private fun taskDto() = ClientTaskDto(
        id = 1,
        clientId = 3,
        title = "Bericht",
        description = "",
        dueDate = LocalDate.of(2026, 3, 20),
        createdAt = LocalDateTime.of(2026, 3, 1, 9, 0),
        createdById = 7,
        createdByName = "Anna Autorin",
        done = false,
        overdue = false,
        completedById = null,
        completedByName = null,
        completedOn = null,
        completedAt = null,
        completionComment = null
    )

    private fun clientDto() = ClientDto().apply {
        id = 3
        firstName = "Max"
        lastName = "Mustermann"
        institution.id = 5
    }
}
