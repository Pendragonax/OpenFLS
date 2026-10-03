package de.vinz.openfls.domains.clientTasks

import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompleteRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompleteResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompletedPageResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCreateRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCreateResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskDeleteResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskPageResponse
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskResponse
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskUpdateRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskUpdateResult
import de.vinz.openfls.domains.clientTasks.service.ClientTaskService
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
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

@WebMvcTest(ClientTaskController::class)
@AutoConfigureMockMvc(addFilters = false)
class ClientTaskControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var clientTaskService: ClientTaskService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    private val createRequest = ClientTaskCreateRequest(3, "Bericht", "", LocalDate.of(2026, 3, 20))
    private val createJson = """{"clientId":3,"title":"Bericht","description":"","dueDate":"2026-03-20"}"""
    private val updateRequest = ClientTaskUpdateRequest("Bericht", "Neu", LocalDate.of(2026, 3, 20))
    private val updateJson = """{"title":"Bericht","description":"Neu","dueDate":"2026-03-20"}"""
    private val completeRequest = ClientTaskCompleteRequest("erledigt", LocalDate.of(2026, 3, 10))
    private val completeJson = """{"comment":"erledigt","completedOn":"2026-03-10"}"""

    private fun status(path: String, json: String): Int = mockMvc.post(path) {
        contentType = MediaType.APPLICATION_JSON
        content = json
    }.andReturn().response.status

    @Test
    fun create_success_returnsOk() {
        given(clientTaskService.create(createRequest)).willReturn(ClientTaskCreateResult.Success(taskResponse()))

        val result = mockMvc.post("/client_tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = createJson
        }.andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"title\":\"Bericht\"")
    }

    @Test
    fun create_unknownClient_returnsNotFound() {
        given(clientTaskService.create(createRequest)).willReturn(ClientTaskCreateResult.ClientNotFound)

        assertThat(status("/client_tasks", createJson)).isEqualTo(404)
    }

    @Test
    fun create_blankTitle_returnsBadRequestWithoutCallingTheService() {
        val json = """{"clientId":3,"title":" ","description":"","dueDate":"2026-03-20"}"""

        assertThat(status("/client_tasks", json)).isEqualTo(400)
    }

    @Test
    fun getOpenTasksByClientId_returnsTheTasks() {
        given(clientTaskService.getOpenTasksByClientId(3L)).willReturn(listOf(taskResponse()))

        val result = mockMvc.get("/client_tasks/client/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"title\":\"Bericht\"")
    }

    @Test
    fun getOpenTasksByClientId_unknownClient_returnsNotFound() {
        given(clientTaskService.getOpenTasksByClientId(3L)).willReturn(null)

        assertThat(mockMvc.get("/client_tasks/client/3").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun complete_success_returnsOk() {
        given(clientTaskService.complete(1L, completeRequest))
            .willReturn(ClientTaskCompleteResult.Success(taskResponse().copy(done = true, completionComment = "erledigt")))

        val result = mockMvc.post("/client_tasks/1/complete") {
            contentType = MediaType.APPLICATION_JSON
            content = completeJson
        }.andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"done\":true")
    }

    @Test
    fun complete_unknownTask_returnsNotFound() {
        given(clientTaskService.complete(1L, completeRequest)).willReturn(ClientTaskCompleteResult.NotFound)

        assertThat(status("/client_tasks/1/complete", completeJson)).isEqualTo(404)
    }

    @Test
    fun complete_alreadyCompleted_returnsConflict() {
        given(clientTaskService.complete(1L, completeRequest)).willReturn(ClientTaskCompleteResult.AlreadyCompleted)

        assertThat(status("/client_tasks/1/complete", completeJson)).isEqualTo(409)
    }

    @Test
    fun change_usesPathIdAndDedicatedActionEndpoint() {
        given(clientTaskService.update(1L, updateRequest)).willReturn(ClientTaskUpdateResult.Success(taskResponse()))

        val result = mockMvc.put("/client_tasks/1/change") {
            contentType = MediaType.APPLICATION_JSON
            content = updateJson
        }.andReturn()

        assertThat(result.response.status).isEqualTo(200)
    }

    @Test
    fun change_unknownTask_returnsNotFound() {
        given(clientTaskService.update(1L, updateRequest)).willReturn(ClientTaskUpdateResult.NotFound)

        val result = mockMvc.put("/client_tasks/1/change") {
            contentType = MediaType.APPLICATION_JSON
            content = updateJson
        }.andReturn()

        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun change_completedTask_returnsConflict() {
        given(clientTaskService.update(1L, updateRequest)).willReturn(ClientTaskUpdateResult.AlreadyCompleted)

        val result = mockMvc.put("/client_tasks/1/change") {
            contentType = MediaType.APPLICATION_JSON
            content = updateJson
        }.andReturn()

        assertThat(result.response.status).isEqualTo(409)
    }

    @Test
    fun completed_returnsRequestedPage() {
        given(clientTaskService.getCompletedTasksByClientId(3L, 1, 10)).willReturn(
            ClientTaskCompletedPageResult.Success(
                ClientTaskPageResponse(listOf(taskResponse().copy(done = true)), 1, 10, 11, 2)
            )
        )

        val result = mockMvc.get("/client_tasks/client/3/completed?page=1&size=10").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"totalElements\":11")
    }

    @Test
    fun completed_unknownClient_returnsNotFound() {
        given(clientTaskService.getCompletedTasksByClientId(3L, 0, 10))
            .willReturn(ClientTaskCompletedPageResult.ClientNotFound)

        assertThat(mockMvc.get("/client_tasks/client/3/completed").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun completed_invalidPagination_returnsBadRequest() {
        given(clientTaskService.getCompletedTasksByClientId(3L, 0, 500))
            .willReturn(ClientTaskCompletedPageResult.InvalidPagination)

        val result = mockMvc.get("/client_tasks/client/3/completed?size=500").andReturn()

        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun delete_removesTheTask() {
        given(clientTaskService.delete(1L)).willReturn(ClientTaskDeleteResult.Success(taskResponse()))

        val result = mockMvc.delete("/client_tasks/1").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":1")
    }

    @Test
    fun delete_unknownTask_returnsNotFound() {
        given(clientTaskService.delete(1L)).willReturn(ClientTaskDeleteResult.NotFound)

        assertThat(mockMvc.delete("/client_tasks/1").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun getHistory_unknownTask_returnsNotFound() {
        given(clientTaskService.getAuditHistoryByTaskId(1L)).willReturn(null)

        assertThat(mockMvc.get("/client_tasks/1/history").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun getHistory_existingTask_returnsTheAuditTrail() {
        given(clientTaskService.getAuditHistoryByTaskId(1L)).willReturn(emptyList())

        val result = mockMvc.get("/client_tasks/1/history").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).isEqualTo("[]")
    }

    private fun taskResponse() = ClientTaskResponse(
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
}
