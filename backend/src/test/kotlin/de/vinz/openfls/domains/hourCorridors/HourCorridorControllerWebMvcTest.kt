package de.vinz.openfls.domains.hourCorridors

import com.fasterxml.jackson.databind.ObjectMapper
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.hourCorridors.dtos.CreateHourCorridorDto
import de.vinz.openfls.domains.hourCorridors.dtos.HourCorridorDto
import de.vinz.openfls.domains.hourCorridors.dtos.UpdateHourCorridorDto
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doThrow
import org.mockito.kotlin.any
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

@WebMvcTest(HourCorridorController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class HourCorridorControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var hourCorridorService: HourCorridorService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun create_nonAdmin_returnsForbidden() {
        // Given
        given(accessService.isAdmin()).willReturn(false)

        // When
        val result = mockMvc.post("/hour_corridors") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                CreateHourCorridorDto(
                    title = "5 bis 10",
                    weeklyMinutesFrom = 300,
                    weeklyMinutesTill = 600,
                    hourTypeId = 1
                )
            )
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(403)
    }

    @Test
    fun create_admin_returnsCreatedDto() {
        // Given
        val dto = HourCorridorDto(
            id = 5,
            title = "5 bis 10",
            weeklyMinutesFrom = 300,
            weeklyMinutesTill = 600,
            hourTypeId = 1,
            hourTypeTitle = "Fachleistungsstunde"
        )
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.create(any())).willReturn(dto)

        // When
        val result = mockMvc.post("/hour_corridors") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                CreateHourCorridorDto(
                    title = "5 bis 10",
                    weeklyMinutesFrom = 300,
                    weeklyMinutesTill = 600,
                    hourTypeId = 1
                )
            )
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":5")
        assertThat(result.response.contentAsString).contains("\"hourTypeTitle\":\"Fachleistungsstunde\"")
    }

    @Test
    fun getAll_admin_returnsDtos() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.getAll()).willReturn(
            listOf(
                HourCorridorDto(id = 1, title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourTypeId = 1)
            )
        )

        // When
        val result = mockMvc.get("/hour_corridors").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"title\":\"5 bis 10\"")
    }

    @Test
    fun countByAssistancePlan_admin_returnsCount() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.countByAssistancePlan(11L)).willReturn(2)

        // When
        val result = mockMvc.get("/hour_corridors/count/assistance_plan/11").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).isEqualTo("2")
    }

    @Test
    fun delete_referencedCorridor_returnsBadRequest() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.existsById(3L)).willReturn(true)
        given(hourCorridorService.getDtoById(3L)).willReturn(
            HourCorridorDto(id = 3, title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourTypeId = 1)
        )
        doThrow(IllegalArgumentException("hour corridor is used by 1 assistance plans"))
            .`when`(hourCorridorService)
            .delete(3L)

        // When
        val result = mockMvc.delete("/hour_corridors/3").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains("Die übergebenen Parameter sind nicht korrekt")
    }

    @Test
    fun update_admin_returnsUpdatedDto() {
        // Given
        val dto = HourCorridorDto(
            id = 7,
            title = "6 bis 12",
            weeklyMinutesFrom = 360,
            weeklyMinutesTill = 720,
            hourTypeId = 2,
            hourTypeTitle = "Fachleistungsstunde"
        )
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.existsById(7L)).willReturn(true)
        given(hourCorridorService.update(any())).willReturn(dto)

        // When
        val result = mockMvc.put("/hour_corridors/7") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                UpdateHourCorridorDto(
                    id = 7,
                    title = "6 bis 12",
                    weeklyMinutesFrom = 360,
                    weeklyMinutesTill = 720,
                    hourTypeId = 2
                )
            )
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":7")
        assertThat(result.response.contentAsString).contains("\"weeklyMinutesFrom\":360")
    }
}
