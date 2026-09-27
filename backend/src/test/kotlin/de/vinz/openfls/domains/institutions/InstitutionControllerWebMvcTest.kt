package de.vinz.openfls.domains.institutions

import de.vinz.openfls.domains.institutions.dto.InstitutionResponse
import de.vinz.openfls.domains.institutions.dto.InstitutionUpdateResult
import de.vinz.openfls.domains.institutions.dto.InstitutionDeleteResult
import de.vinz.openfls.domains.institutions.dto.InstitutionWithPermissionsResponse
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.services.PerformanceLoggingService
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

@WebMvcTest(InstitutionController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class InstitutionControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun getById_missingInstitution_returnsNotFound() {
        // Given
        given(institutionService.getWithPermissionsById(7L)).willReturn(null)

        // When
        val result = mockMvc.get("/institutions/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun update_missingInstitution_returnsNotFound() {
        // Given
        given(institutionService.update(any())).willReturn(InstitutionUpdateResult.NotFound)

        // When
        val result = mockMvc.put("/institutions/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":7,"name":"Inst"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun update_admin_returnsUpdatedDto() {
        // Given
        given(institutionService.update(any())).willReturn(
            InstitutionUpdateResult.Success(InstitutionWithPermissionsResponse(id = 7, name = "Inst"))
        )

        // When
        val result = mockMvc.put("/institutions/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":7,"name":"Inst"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":7")
    }

    @Test
    fun update_pathIdDiffersFromRequestId_returnsBadRequest() {
        // When
        val result = mockMvc.put("/institutions/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":8,"name":"Inst"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun delete_missingInstitution_returnsNotFound() {
        // Given
        given(institutionService.delete(7L)).willReturn(InstitutionDeleteResult.NotFound)

        // When
        val result = mockMvc.delete("/institutions/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun delete_existingInstitution_returnsDeletedDto() {
        // Given
        given(institutionService.delete(7L)).willReturn(
            InstitutionDeleteResult.Success(InstitutionWithPermissionsResponse(id = 7, name = "Inst"))
        )

        // When
        val result = mockMvc.delete("/institutions/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":7")
    }

    @Test
    fun getAllReadable_returnsOnlyReadableInstitutions() {
        // Given
        given(institutionService.getAll()).willReturn(
            listOf(InstitutionResponse(id = 1, name = "Readable"), InstitutionResponse(id = 2, name = "Hidden"))
        )
        given(accessService.canReadEntries(1L)).willReturn(true)
        given(accessService.canReadEntries(2L)).willReturn(false)

        // When
        val result = mockMvc.get("/institutions/readable").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("Readable").doesNotContain("Hidden")
    }
}
