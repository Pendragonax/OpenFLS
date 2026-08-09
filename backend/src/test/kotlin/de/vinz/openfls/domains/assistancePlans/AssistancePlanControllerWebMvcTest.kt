package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanPreviewDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanExistingDto
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanEvaluationLeftService
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanEvaluationService
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanPreviewService
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.clients.ClientService
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
    lateinit var assistancePlanEvaluationService: AssistancePlanEvaluationService

    @MockitoBean
    lateinit var assistancePlanEvaluationLeftService: AssistancePlanEvaluationLeftService

    @MockitoBean
    lateinit var assistancePlanPreviewService: AssistancePlanPreviewService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var userService: UserService

    @MockitoBean
    lateinit var clientService: ClientService

    @Test
    fun getPreviewByClientId_returnsPreviewDtos() {
        given(userService.getUserId()).willReturn(44L)
        given(assistancePlanPreviewService.getPreviewDtosByClientId(3L, 44L, false))
            .willReturn(listOf(previewDto(id = 1L, isFavorite = true)))

        val result = mockMvc.get("/assistance_plans/client/3/preview").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":1")
        assertThat(result.response.contentAsString).contains("\"isFavorite\":true")
    }

    @Test
    fun getPreviewByInstitutionId_returnsPreviewDtos() {
        given(userService.getUserId()).willReturn(44L)
        given(assistancePlanPreviewService.getPreviewDtosByInstitutionId(9L, 44L, false))
            .willReturn(listOf(previewDto(id = 2L, isFavorite = false)))

        val result = mockMvc.get("/assistance_plans/institution/9/preview").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":2")
        assertThat(result.response.contentAsString).contains("\"isFavorite\":false")
    }

    @Test
    fun getById_admin_returnsAssistancePlanDto() {
        val planId = 12L
        given(accessService.isAdmin()).willReturn(true)
        given(accessService.getLeadingInstitutionIds()).willReturn(emptyList())
        given(assistancePlanService.getAssistancePlanDtoById(planId, true, emptyList()))
            .willReturn(
                AssistancePlanDto().apply {
                    id = planId
                    clientId = 33
                    institutionId = 44
                    sponsorId = 55
                    clientArchived = true
                }
            )

        val result = mockMvc.get("/assistance_plans/$planId").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":12")
        assertThat(result.response.contentAsString).contains("\"clientArchived\":true")
    }

    @Test
    fun getFavoritePreviewsByLoggedInUser_returnsPreviewDtos() {
        given(userService.getUserId()).willReturn(44L)
        given(accessService.getLeadingInstitutionIds()).willReturn(emptyList())
        given(assistancePlanPreviewService.getFavoritePreviewDtosByEmployeeId(44L, false, emptyList()))
            .willReturn(listOf(previewDto(id = 4L, isFavorite = true)))

        val result = mockMvc.get("/assistance_plans/favorites/preview").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":4")
        assertThat(result.response.contentAsString).contains("\"isFavorite\":true")
    }

    @Test
    fun getFavoritePreviewsByLoggedInUser_serviceThrows_returnsBadRequest() {
        given(userService.getUserId()).willReturn(44L)
        given(accessService.getLeadingInstitutionIds()).willReturn(emptyList())
        doThrow(IllegalArgumentException("boom"))
            .`when`(assistancePlanPreviewService)
            .getFavoritePreviewDtosByEmployeeId(44L, false, emptyList())

        val result = mockMvc.get("/assistance_plans/favorites/preview").andReturn()

        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun getExistingByClientId_returnsExistingDtos() {
        given(assistancePlanPreviewService.getExistingDtosByClientId(3L, false))
            .willReturn(
                listOf(
                    AssistancePlanExistingDto(
                        id = 99L,
                        start = LocalDate.of(2026, 1, 1),
                        end = LocalDate.of(2026, 3, 31),
                        sponsorName = "LWV",
                        clientArchived = false
                    )
                )
            )

        val result = mockMvc.get("/assistance_plans/client/3/existing").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":99")
        assertThat(result.response.contentAsString).contains("\"sponsorName\":\"LWV\"")
    }

    private fun previewDto(id: Long, isFavorite: Boolean): AssistancePlanPreviewDto {
        return AssistancePlanPreviewDto(
            id = id,
            clientId = 11,
            institutionId = 12,
            sponsorId = 13,
            clientFirstname = "Max",
            clientLastname = "Mustermann",
            institutionName = "Schule",
            sponsorName = "Kostentraeger",
            clientArchived = false,
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 12, 31),
            isActive = true,
            isFavorite = isFavorite,
            hasIllegalHours = false,
            hourMode = AssistancePlanHourMode.EXACT,
            approvedHoursFrom = 7.0,
            approvedHoursTo = 7.0,
            approvedHoursPerWeek = 7.0,
            approvedHoursThisYearFrom = 366.0,
            approvedHoursThisYearTill = 366.0,
            approvedHoursThisYear = 366.0,
            executedHoursThisYear = 100.0,
            approvedHoursLeftThisYear = 266.0
        )
    }
}
