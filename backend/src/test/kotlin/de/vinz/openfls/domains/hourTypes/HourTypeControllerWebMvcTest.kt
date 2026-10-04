package de.vinz.openfls.domains.hourTypes

import de.vinz.openfls.domains.hourTypes.dto.HourTypeDeleteResult
import de.vinz.openfls.domains.hourTypes.dto.HourTypeResponse
import de.vinz.openfls.domains.hourTypes.dto.HourTypeUpdateResult
import de.vinz.openfls.domains.hourTypes.service.HourTypeService
import de.vinz.openfls.common.web.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

@WebMvcTest(HourTypeController::class)
@AutoConfigureMockMvc(addFilters = false)
class HourTypeControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var hourTypeService: HourTypeService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun getById_missingSponsor_returnsNotFound() {
        // Given
        given(hourTypeService.getById(7L)).willReturn(null)

        // When
        val result = mockMvc.get("/hour_types/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
        assertThat(result.response.contentAsString).isEqualTo("Type of hour with id 7 does not exists.")
    }

    @Test
    fun update_missingSponsor_returnsNotFound() {
        // Given
        given(hourTypeService.update(any())).willReturn(HourTypeUpdateResult.NotFound)

        // When
        val result = mockMvc.put("/hour_types/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":7,"title":"Standard"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun update_admin_returnsUpdatedDto() {
        // Given
        given(hourTypeService.update(any())).willReturn(
            HourTypeUpdateResult.Success(HourTypeResponse(id = 7, title = "Standard", price = 3.0))
        )

        // When
        val result = mockMvc.put("/hour_types/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":7,"title":"Standard","price":3.0}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":7")
    }

    @Test
    fun update_pathIdDiffersFromRequestId_returnsBadRequest() {
        // When
        val result = mockMvc.put("/hour_types/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":8,"title":"Standard"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun delete_missingHourType_returnsNotFound() {
        // Given
        given(hourTypeService.delete(7L)).willReturn(HourTypeDeleteResult.NotFound)

        // When
        val result = mockMvc.delete("/hour_types/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun delete_existingHourType_returnsDeletedDto() {
        // Given
        given(hourTypeService.delete(7L)).willReturn(
            HourTypeDeleteResult.Success(HourTypeResponse(id = 7, title = "Standard", price = 3.0))
        )

        // When
        val result = mockMvc.delete("/hour_types/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":7")
    }

    @Test
    fun getAll_returnsHourTypes() {
        // Given
        given(hourTypeService.getAll()).willReturn(listOf(HourTypeResponse(id = 1, title = "Standard", price = 1.0)))

        // When
        val result = mockMvc.get("/hour_types").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"title\":\"Standard\"")
    }
}
