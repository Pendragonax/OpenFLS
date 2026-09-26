package de.vinz.openfls.domains.categories

import de.vinz.openfls.domains.categories.dtos.CategoryTemplateWithCategoriesResponse
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
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@WebMvcTest(CategoryTemplateController::class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryTemplateControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var categoryTemplateService: CategoryTemplateService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun getById_missingTemplate_returnsNotFound() {
        // Given
        given(categoryTemplateService.getById(7L)).willReturn(null)

        // When
        val result = mockMvc.get("/categories/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun update_missingTemplate_returnsNotFound() {
        // Given
        given(categoryTemplateService.existsById(7L)).willReturn(false)

        // When
        val result = mockMvc.put("/categories/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":7,"title":"T"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun update_pathIdDiffersFromRequestId_returnsBadRequest() {
        // When
        val result = mockMvc.put("/categories/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":8,"title":"T"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun delete_missingTemplate_returnsNotFoundWithoutDeleting() {
        // Given
        given(categoryTemplateService.getById(7L)).willReturn(null)

        // When
        val result = mockMvc.delete("/categories/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
        verify(categoryTemplateService, never()).delete(7L)
    }

    @Test
    fun create_categoryWithoutTitle_returnsBadRequest() {
        // When
        val result = mockMvc.post("/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"T","categories":[{"title":"","shortcut":"C"}]}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun getAll_returnsTemplates() {
        // Given
        given(categoryTemplateService.getAll())
            .willReturn(listOf(CategoryTemplateWithCategoriesResponse(id = 1, title = "Template")))

        // When
        val result = mockMvc.get("/categories").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"title\":\"Template\"")
    }
}
