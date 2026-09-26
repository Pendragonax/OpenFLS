package de.vinz.openfls.domains.sponsors

import de.vinz.openfls.domains.sponsors.dtos.SponsorResponse
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

@WebMvcTest(SponsorController::class)
@AutoConfigureMockMvc(addFilters = false)
class SponsorControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var sponsorService: SponsorService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun getById_missingSponsor_returnsNotFound() {
        // Given
        given(sponsorService.getById(7L)).willReturn(null)

        // When
        val result = mockMvc.get("/sponsors/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
        assertThat(result.response.contentAsString).isEqualTo("sponsor not found")
    }

    @Test
    fun update_missingSponsor_returnsNotFound() {
        // Given
        given(sponsorService.existsById(7L)).willReturn(false)

        // When
        val result = mockMvc.put("/sponsors/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":7,"name":"Sponsor"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun update_pathIdDiffersFromRequestId_returnsBadRequest() {
        // When
        val result = mockMvc.put("/sponsors/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":8,"name":"Sponsor"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun delete_missingSponsor_returnsNotFoundWithoutDeleting() {
        // Given
        given(sponsorService.getById(7L)).willReturn(null)

        // When
        val result = mockMvc.delete("/sponsors/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
        verify(sponsorService, never()).delete(7L)
    }

    @Test
    fun getAll_returnsSponsors() {
        // Given
        given(sponsorService.getAll()).willReturn(listOf(SponsorResponse(id = 1, name = "Sponsor")))

        // When
        val result = mockMvc.get("/sponsors").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"name\":\"Sponsor\"")
    }
}
