package de.vinz.openfls.domains.clients.dashboard

import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.clients.dashboard.dtos.ClientDashboardAccess
import de.vinz.openfls.domains.clients.dashboard.dtos.ClientDashboardDto
import de.vinz.openfls.domains.clients.dtos.ClientDto
import de.vinz.openfls.domains.employees.dto.EmployeeFavoriteResult
import de.vinz.openfls.domains.employees.service.EmployeeFavoriteService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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
    lateinit var clientService: ClientService

    @MockitoBean
    lateinit var employeeFavoriteService: EmployeeFavoriteService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @BeforeEach
    fun setUp() {
        given(accessService.getId()).willReturn(7L)
        given(clientService.getById(eq(3L), any(), any())).willReturn(ClientDto().apply {
            id = 3
            firstName = "Max"
            lastName = "Mustermann"
            institution.id = 5
        })
    }

    @Test
    fun getDashboard_employeeWithReadRights_getsGrantedSections() {
        given(accessService.isAdmin()).willReturn(false)
        given(accessService.isLeader(5L)).willReturn(false)
        given(accessService.isAffiliated(5L)).willReturn(false)
        given(accessService.getReadRightsInstitutionIds()).willReturn(listOf(5L))
        given(accessService.canWriteEntries(5L)).willReturn(true)
        given(accessService.canModifyClient(3L)).willReturn(false)
        given(clientDashboardService.getDashboard(any(), any(), any(), any(), any(), any(), any(), any()))
            .willReturn(dashboardDto(ClientDashboardAccess.GRANTED))

        val result = mockMvc.get("/client_dashboards/client/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"assistancePlanAccess\":\"GRANTED\"")
        verify(clientDashboardService).getDashboard(
            clientId = eq(3L),
            employeeId = eq(7L),
            isAdmin = eq(false),
            includeArchived = eq(false),
            canReadDocumentation = eq(true),
            canWriteEntries = eq(true),
            canModifyClient = eq(false),
            readableInstitutionIds = eq(listOf(5L))
        )
    }

    @Test
    fun getDashboard_employeeWithoutAnyRightOnTheInstitution_getsDeniedSections() {
        given(accessService.isAdmin()).willReturn(false)
        given(accessService.isLeader(5L)).willReturn(false)
        given(accessService.isAffiliated(5L)).willReturn(false)
        given(accessService.getReadRightsInstitutionIds()).willReturn(listOf(9L))
        given(clientDashboardService.getDashboard(any(), any(), any(), any(), any(), any(), any(), any()))
            .willReturn(dashboardDto(ClientDashboardAccess.DENIED))

        val result = mockMvc.get("/client_dashboards/client/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"servicesAccess\":\"DENIED\"")
        verify(clientDashboardService).getDashboard(
            clientId = eq(3L),
            employeeId = eq(7L),
            isAdmin = eq(false),
            includeArchived = eq(false),
            canReadDocumentation = eq(false),
            canWriteEntries = eq(false),
            canModifyClient = eq(false),
            readableInstitutionIds = eq(listOf(9L))
        )
    }

    @Test
    fun getDashboard_unknownClient_returnsBadRequest() {
        given(clientService.getById(eq(404L), any(), any())).willReturn(null)

        val result = mockMvc.get("/client_dashboards/client/404").andReturn()

        assertThat(result.response.status).isEqualTo(400)
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

    private fun dashboardDto(access: ClientDashboardAccess) = ClientDashboardDto(
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
