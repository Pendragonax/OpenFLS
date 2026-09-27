package de.vinz.openfls.domains.hourCorridors

import com.fasterxml.jackson.databind.ObjectMapper
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorCreateRequest
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorCreateResult
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorDeleteResult
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorResponse
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorUpdateRequest
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorUpdateResult
import de.vinz.openfls.domains.hourCorridors.service.HourCorridorService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
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

@WebMvcTest(HourCorridorController::class)
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
                HourCorridorCreateRequest(title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourTypeId = 1)
            )
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(403)
        verify(hourCorridorService, never()).create(any())
    }

    @Test
    fun create_admin_returnsCreatedDto() {
        // Given
        val response = HourCorridorResponse(
            id = 5, title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600,
            hourTypeId = 1, hourTypeTitle = "Fachleistungsstunde"
        )
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.create(any())).willReturn(HourCorridorCreateResult.Success(response))

        // When
        val result = mockMvc.post("/hour_corridors") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                HourCorridorCreateRequest(title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourTypeId = 1)
            )
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":5")
        assertThat(result.response.contentAsString).contains("\"hourTypeTitle\":\"Fachleistungsstunde\"")
    }

    @Test
    fun create_tillBeforeFrom_returnsBadRequest() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.create(any())).willReturn(HourCorridorCreateResult.InvalidRange("till before from"))

        // When
        val result = mockMvc.post("/hour_corridors") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                HourCorridorCreateRequest(title = "Ungültig", weeklyMinutesFrom = 600, weeklyMinutesTill = 300, hourTypeId = 1)
            )
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains("till before from")
    }

    @Test
    fun create_unknownHourType_returnsBadRequest() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.create(any())).willReturn(
            HourCorridorCreateResult.HourTypeNotFound("hour type with id 9 not found")
        )

        // When
        val result = mockMvc.post("/hour_corridors") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                HourCorridorCreateRequest(title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourTypeId = 9)
            )
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains("hour type with id 9 not found")
    }

    @Test
    fun getAll_admin_returnsDtos() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.getAll()).willReturn(
            listOf(HourCorridorResponse(id = 1, title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourTypeId = 1))
        )

        // When
        val result = mockMvc.get("/hour_corridors").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"title\":\"5 bis 10\"")
    }

    @Test
    fun getAll_nonAdmin_returnsDtos() {
        given(accessService.isAdmin()).willReturn(false)
        given(hourCorridorService.getAll()).willReturn(
            listOf(HourCorridorResponse(id = 1, title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourTypeId = 1))
        )

        val result = mockMvc.get("/hour_corridors").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"title\":\"5 bis 10\"")
    }

    @Test
    fun getById_knownCorridor_returnsDto() {
        given(hourCorridorService.getById(3L)).willReturn(
            HourCorridorResponse(id = 3, title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourTypeId = 1)
        )

        val result = mockMvc.get("/hour_corridors/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":3")
    }

    @Test
    fun getById_unknownCorridor_returnsNotFound() {
        given(hourCorridorService.getById(9L)).willReturn(null)

        val result = mockMvc.get("/hour_corridors/9").andReturn()

        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun countByAssistancePlan_admin_returnsCount() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.countAssistancePlansByHourCorridorId(11L)).willReturn(2)

        // When
        val result = mockMvc.get("/hour_corridors/count/assistance_plan/11").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).isEqualTo("2")
    }

    @Test
    fun update_admin_returnsUpdatedDto() {
        // Given
        val response = HourCorridorResponse(
            id = 7, title = "6 bis 12", weeklyMinutesFrom = 360, weeklyMinutesTill = 720,
            hourTypeId = 2, hourTypeTitle = "Fachleistungsstunde"
        )
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.update(any())).willReturn(HourCorridorUpdateResult.Success(response))

        // When
        val result = mockMvc.put("/hour_corridors/7") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                HourCorridorUpdateRequest(id = 7, title = "6 bis 12", weeklyMinutesFrom = 360, weeklyMinutesTill = 720, hourTypeId = 2)
            )
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":7")
        assertThat(result.response.contentAsString).contains("\"weeklyMinutesFrom\":360")
    }

    @Test
    fun update_unknownCorridor_returnsNotFound() {
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.update(any())).willReturn(HourCorridorUpdateResult.NotFound)

        val result = mockMvc.put("/hour_corridors/7") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                HourCorridorUpdateRequest(id = 7, title = "6 bis 12", weeklyMinutesFrom = 360, weeklyMinutesTill = 720, hourTypeId = 2)
            )
        }.andReturn()

        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun update_tillBeforeFrom_returnsBadRequest() {
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.update(any())).willReturn(HourCorridorUpdateResult.InvalidRange("till before from"))

        val result = mockMvc.put("/hour_corridors/7") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                HourCorridorUpdateRequest(id = 7, title = "6 bis 12", weeklyMinutesFrom = 720, weeklyMinutesTill = 360, hourTypeId = 2)
            )
        }.andReturn()

        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains("till before from")
    }

    @Test
    fun update_unknownHourType_returnsBadRequest() {
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.update(any())).willReturn(
            HourCorridorUpdateResult.HourTypeNotFound("hour type with id 9 not found")
        )

        val result = mockMvc.put("/hour_corridors/7") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                HourCorridorUpdateRequest(id = 7, title = "6 bis 12", weeklyMinutesFrom = 360, weeklyMinutesTill = 720, hourTypeId = 9)
            )
        }.andReturn()

        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains("hour type with id 9 not found")
    }

    @Test
    fun update_pathIdDiffersFromRequestId_returnsBadRequest() {
        given(accessService.isAdmin()).willReturn(true)

        val result = mockMvc.put("/hour_corridors/8") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                HourCorridorUpdateRequest(id = 7, title = "6 bis 12", weeklyMinutesFrom = 360, weeklyMinutesTill = 720, hourTypeId = 2)
            )
        }.andReturn()

        assertThat(result.response.status).isEqualTo(400)
        verify(hourCorridorService, never()).update(any())
    }

    @Test
    fun update_nonAdmin_returnsForbidden() {
        given(accessService.isAdmin()).willReturn(false)

        val result = mockMvc.put("/hour_corridors/7") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                HourCorridorUpdateRequest(id = 7, title = "6 bis 12", weeklyMinutesFrom = 360, weeklyMinutesTill = 720, hourTypeId = 2)
            )
        }.andReturn()

        assertThat(result.response.status).isEqualTo(403)
        verify(hourCorridorService, never()).update(any())
    }

    @Test
    fun delete_referencedCorridor_returnsConflict() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.delete(3L)).willReturn(HourCorridorDeleteResult.Conflict(1))

        // When
        val result = mockMvc.delete("/hour_corridors/3").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(409)
        assertThat(result.response.contentAsString).contains("used by 1 assistance plans")
    }

    @Test
    fun delete_unknownCorridor_returnsNotFound() {
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.delete(3L)).willReturn(HourCorridorDeleteResult.NotFound)

        val result = mockMvc.delete("/hour_corridors/3").andReturn()

        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun delete_unusedCorridor_returnsDeletedCorridor() {
        given(accessService.isAdmin()).willReturn(true)
        given(hourCorridorService.delete(3L)).willReturn(
            HourCorridorDeleteResult.Success(
                HourCorridorResponse(id = 3, title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourTypeId = 1)
            )
        )

        val result = mockMvc.delete("/hour_corridors/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":3")
    }

    @Test
    fun delete_nonAdmin_returnsForbidden() {
        given(accessService.isAdmin()).willReturn(false)

        val result = mockMvc.delete("/hour_corridors/3").andReturn()

        assertThat(result.response.status).isEqualTo(403)
        verify(hourCorridorService, never()).delete(any())
    }
}
