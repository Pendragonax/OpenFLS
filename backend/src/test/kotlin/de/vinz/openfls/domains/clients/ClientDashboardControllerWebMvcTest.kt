package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.dto.ClientDashboardResult
import de.vinz.openfls.domains.clients.service.ClientDashboardService
import de.vinz.openfls.domains.clients.dto.ClientFavoriteResponse
import de.vinz.openfls.domains.clients.dto.ClientDashboardAccess
import de.vinz.openfls.domains.clients.dto.ClientDashboardResponse
import de.vinz.openfls.domains.employees.dto.EmployeeFavoriteResult
import de.vinz.openfls.domains.employees.service.EmployeeFavoriteService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.common.web.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(ClientDashboardController::class)
@AutoConfigureMockMvc(addFilters = false)
class ClientDashboardControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var clientDashboardService: ClientDashboardService

    @MockitoBean
    lateinit var employeeFavoriteService: EmployeeFavoriteService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @BeforeEach
    fun setUp() {
        given(accessService.getId()).willReturn(7L)
    }

    @Test
    fun getDashboard_knownClient_returnsDashboardWithAccessStates() {
        given(clientDashboardService.getDashboard(3L))
            .willReturn(ClientDashboardResult.Success(dashboardDto(ClientDashboardAccess.GRANTED)))

        val result = mockMvc.get("/client_dashboards/client/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"assistancePlanAccess\":\"GRANTED\"")
    }

    @Test
    fun getDashboard_deniedSections_arePassedOnToTheFrontend() {
        given(clientDashboardService.getDashboard(3L))
            .willReturn(ClientDashboardResult.Success(dashboardDto(ClientDashboardAccess.DENIED)))

        val result = mockMvc.get("/client_dashboards/client/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"servicesAccess\":\"DENIED\"")
    }

    @Test
    fun getDashboard_unknownClient_returnsNotFound() {
        given(clientDashboardService.getDashboard(404L)).willReturn(ClientDashboardResult.NotFound)

        assertThat(mockMvc.get("/client_dashboards/client/404").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun getFavorites_returnsFavoritesOfTheSignedInEmployee() {
        given(clientDashboardService.getFavorites()).willReturn(
            listOf(ClientFavoriteResponse(3L, "Max", "Mustermann", false, 5L, "Inst", true, null, 2, 1))
        )

        val result = mockMvc.get("/client_dashboards/favorites").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"clientId\":3")
    }

    @Test
    fun addFavorite_unknownClient_returnsNotFound() {
        given(employeeFavoriteService.addClientFavorite(7L, 404L)).willReturn(EmployeeFavoriteResult.ClientNotFound)

        val result = mockMvc.post("/client_dashboards/favorites/client/404").andReturn()

        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun addFavorite_knownClient_storesTheFavoriteForTheSignedInEmployee() {
        given(employeeFavoriteService.addClientFavorite(7L, 3L)).willReturn(EmployeeFavoriteResult.Success)

        val result = mockMvc.post("/client_dashboards/favorites/client/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        verify(employeeFavoriteService).addClientFavorite(7L, 3L)
    }

    private fun dashboardDto(access: ClientDashboardAccess) = ClientDashboardResponse(
        clientId = 3,
        firstName = "Max",
        lastName = "Mustermann",
        archived = false,
        institutionId = 5,
        institutionName = "Inst",
        favorite = false,
        canModifyClient = false,
        canWriteEntries = false,
        assistancePlanAccess = access,
        currentAssistancePlan = null,
        assistancePlanCount = 0,
        servicesAccess = access,
        latestServices = emptyList(),
        tasks = emptyList(),
        openTaskCount = 0
    )
}
