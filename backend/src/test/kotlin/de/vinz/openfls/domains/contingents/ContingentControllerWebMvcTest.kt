package de.vinz.openfls.domains.contingents

import de.vinz.openfls.domains.contingents.services.ContingentService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(ContingentController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class ContingentControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var contingentService: ContingentService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun getByInstitutionId_adminWithIncludeArchived_forwardsOptIn() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(contingentService.getByInstitutionId(9L, true)).willReturn(emptyList())

        // When
        val result = mockMvc.get("/contingents/institution/9") {
            param("includeArchivedEmployees", "true")
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        verify(contingentService).getByInstitutionId(9L, true)
    }

    @Test
    fun getByInstitutionId_nonAdminIgnoresOptIn() {
        // Given
        given(accessService.isAdmin()).willReturn(false)
        given(contingentService.getByInstitutionId(9L, false)).willReturn(emptyList())

        // When
        val result = mockMvc.get("/contingents/institution/9") {
            param("includeArchivedEmployees", "true")
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        verify(contingentService).getByInstitutionId(9L, false)
    }

    @Test
    fun getByEmployeeId_withIncludeArchived_forwardsOptIn() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(contingentService.getByEmployeeId(7L, true)).willReturn(emptyList())

        // When
        val result = mockMvc.get("/contingents/employee/7") {
            param("includeArchivedEmployees", "true")
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        verify(contingentService).getByEmployeeId(7L, true)
    }

    @Test
    fun getByEmployeeId_nonAdminIgnoresOptIn() {
        // Given
        given(accessService.isAdmin()).willReturn(false)
        given(contingentService.getByEmployeeId(7L, false)).willReturn(emptyList())

        // When
        val result = mockMvc.get("/contingents/employee/7") {
            param("includeArchivedEmployees", "true")
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        verify(contingentService).getByEmployeeId(7L, false)
    }
}
