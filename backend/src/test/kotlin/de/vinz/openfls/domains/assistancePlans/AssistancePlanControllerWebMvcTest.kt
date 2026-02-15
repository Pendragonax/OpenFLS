package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanPreviewDto
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanEvaluationLeftService
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanPreviewService
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate

@WebMvcTest(AssistancePlanController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class AssistancePlanControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var assistancePlanService: AssistancePlanService

    @MockitoBean
    lateinit var assistancePlanEvaluationLeftService: AssistancePlanEvaluationLeftService

    @MockitoBean
    lateinit var assistancePlanPreviewService: AssistancePlanPreviewService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var userService: UserService

    @Test
    fun getPreviewByClientId_returnsPreviewDtos() {
        given(userService.getUserId()).willReturn(44L)
        given(assistancePlanPreviewService.getPreviewDtosByClientId(3L, 44L))
            .willReturn(listOf(previewDto(id = 1L, isFavorite = true)))

        val result = mockMvc.get("/assistance_plans/client/3/preview").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":1")
        assertThat(result.response.contentAsString).contains("\"isFavorite\":true")
    }

    @Test
    fun getPreviewByInstitutionId_returnsPreviewDtos() {
        given(userService.getUserId()).willReturn(44L)
        given(assistancePlanPreviewService.getPreviewDtosByInstitutionId(9L, 44L))
            .willReturn(listOf(previewDto(id = 2L, isFavorite = false)))

        val result = mockMvc.get("/assistance_plans/institution/9/preview").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":2")
        assertThat(result.response.contentAsString).contains("\"isFavorite\":false")
    }

    @Test
    fun getFavoritePreviewsByLoggedInUser_serviceThrows_returnsBadRequest() {
        given(userService.getUserId()).willReturn(44L)
        doThrow(IllegalArgumentException("boom"))
            .`when`(assistancePlanPreviewService)
            .getFavoritePreviewDtosByEmployeeId(44L)

        val result = mockMvc.get("/assistance_plans/favorites/preview").andReturn()

        assertThat(result.response.status).isEqualTo(400)
    }

    private fun previewDto(id: Long, isFavorite: Boolean): AssistancePlanPreviewDto {
        return AssistancePlanPreviewDto(
            id = id,
            clientFirstname = "Max",
            clientLastname = "Mustermann",
            institutionName = "Schule",
            sponsorName = "Kostentraeger",
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 12, 31),
            isActive = true,
            isFavorite = isFavorite,
            approvedHoursPerWeek = 7.0,
            approvedHoursThisYear = 366.0,
            executedHoursThisYear = 100.0
        )
    }
}
