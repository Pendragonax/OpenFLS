package de.vinz.openfls.domains.clients.service

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanPeriodDto
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanPreviewResponse
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanPreviewService
import de.vinz.openfls.domains.clientTasks.service.ClientTaskService
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCountDto
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskResponse
import de.vinz.openfls.domains.clients.dto.ClientDashboardAccess
import de.vinz.openfls.domains.clients.dto.ClientDashboardResponse
import de.vinz.openfls.domains.clients.dto.ClientDashboardResult
import de.vinz.openfls.domains.clients.repository.ClientDashboardRepository
import de.vinz.openfls.domains.institutions.dto.InstitutionResponse
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.clients.dto.ClientDetailResponse
import de.vinz.openfls.domains.clients.dto.ClientFavoriteRowDto
import de.vinz.openfls.domains.services.dto.ClientLatestServiceResponse
import de.vinz.openfls.domains.services.service.ServiceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ClientDashboardServiceTest {

    private val today: LocalDate = LocalDate.of(2026, 3, 10)
    private val clock: Clock = Clock.fixed(Instant.parse("2026-03-10T08:00:00Z"), ZoneId.of("UTC"))

    private val clientService: ClientService = mock()
    private val clientDashboardRepository: ClientDashboardRepository = mock()
    private val accessService: AccessService = mock()
    private val assistancePlanPreviewService: AssistancePlanPreviewService = mock()
    private val serviceService: ServiceService = mock()
    private val clientTaskService: ClientTaskService = mock()

    private val clientDashboardService = ClientDashboardService(
        clientService,
        clientDashboardRepository,
        accessService,
        assistancePlanPreviewService,
        serviceService,
        clientTaskService,
        clock
    )

    @BeforeEach
    fun setUp() {
        whenever(accessService.getId()).thenReturn(7L)
    }

    @Test
    fun getDashboard_withReadPermission_returnsRunningPlanAndEntries() {
        givenClient()
        givenAccess(readableInstitutionIds = listOf(5L), canWriteEntries = true)
        whenever(clientDashboardRepository.findFavoriteClientIdsByEmployeeId(7L)).thenReturn(listOf(1L))
        whenever(assistancePlanPreviewService.getPreviewsByClientIdAndEmployeeId(1L, 7L, false))
            .thenReturn(listOf(endedPlan(), runningPlan(), futurePlan()))
        whenever(serviceService.getLatestServicesByClientId(any(), any(), any(), any(), any()))
            .thenReturn(listOf(latestService()))
        whenever(clientTaskService.getOpenTasksByClientId(any())).thenReturn(listOf(openTask(), doneTask()))

        val dashboard = dashboardOf(1L)

        assertThat(dashboard.assistancePlanAccess).isEqualTo(ClientDashboardAccess.GRANTED)
        assertThat(dashboard.servicesAccess).isEqualTo(ClientDashboardAccess.GRANTED)
        assertThat(dashboard.canWriteEntries).isTrue()
        assertThat(dashboard.currentAssistancePlan?.id).isEqualTo(runningPlan().id)
        assertThat(dashboard.assistancePlanCount).isEqualTo(3)
        assertThat(dashboard.latestServices).hasSize(1)
        assertThat(dashboard.openTaskCount).isEqualTo(1)
        assertThat(dashboard.favorite).isTrue()
    }

    @Test
    fun getDashboard_withoutAnyRightOnTheInstitution_deniesPlanAndEntriesButKeepsTasks() {
        givenClient()
        givenAccess(readableInstitutionIds = listOf(9L))
        whenever(clientDashboardRepository.findFavoriteClientIdsByEmployeeId(7L)).thenReturn(emptyList())
        whenever(clientTaskService.getOpenTasksByClientId(any())).thenReturn(listOf(openTask()))

        val dashboard = dashboardOf(1L)

        assertThat(dashboard.assistancePlanAccess).isEqualTo(ClientDashboardAccess.DENIED)
        assertThat(dashboard.servicesAccess).isEqualTo(ClientDashboardAccess.DENIED)
        assertThat(dashboard.currentAssistancePlan).isNull()
        assertThat(dashboard.assistancePlanCount).isZero()
        assertThat(dashboard.latestServices).isEmpty()
        assertThat(dashboard.tasks).hasSize(1)
        assertThat(dashboard.favorite).isFalse()

        verify(assistancePlanPreviewService, never()).getPreviewsByClientIdAndEmployeeId(any(), any(), any())
        verify(serviceService, never()).getLatestServicesByClientId(any(), any(), any(), any(), any())
    }

    @Test
    fun getDashboard_affiliatedEmployeeWithoutReadRights_mayReadTheDocumentation() {
        givenClient()
        givenAccess(affiliated = true)
        whenever(clientDashboardRepository.findFavoriteClientIdsByEmployeeId(7L)).thenReturn(emptyList())
        whenever(assistancePlanPreviewService.getPreviewsByClientIdAndEmployeeId(1L, 7L, false)).thenReturn(emptyList())
        whenever(serviceService.getLatestServicesByClientId(any(), any(), any(), any(), any())).thenReturn(emptyList())
        whenever(clientTaskService.getOpenTasksByClientId(any())).thenReturn(emptyList())

        assertThat(dashboardOf(1L).assistancePlanAccess).isEqualTo(ClientDashboardAccess.GRANTED)
    }

    @Test
    fun getDashboard_leaderOfTheInstitution_alsoSeesArchivedPlans() {
        givenClient()
        givenAccess(leader = true)
        whenever(clientDashboardRepository.findFavoriteClientIdsByEmployeeId(7L)).thenReturn(emptyList())
        whenever(assistancePlanPreviewService.getPreviewsByClientIdAndEmployeeId(1L, 7L, true)).thenReturn(emptyList())
        whenever(serviceService.getLatestServicesByClientId(any(), any(), any(), any(), any())).thenReturn(emptyList())
        whenever(clientTaskService.getOpenTasksByClientId(any())).thenReturn(emptyList())

        dashboardOf(1L)

        verify(assistancePlanPreviewService).getPreviewsByClientIdAndEmployeeId(1L, 7L, true)
    }

    @Test
    fun getDashboard_withoutRunningPlan_fallsBackToPlanThatEndedLast() {
        givenClient()
        givenAccess(readableInstitutionIds = listOf(5L))
        whenever(clientDashboardRepository.findFavoriteClientIdsByEmployeeId(7L)).thenReturn(emptyList())
        whenever(assistancePlanPreviewService.getPreviewsByClientIdAndEmployeeId(1L, 7L, false))
            .thenReturn(listOf(endedPlan(), endedPlan().copy(id = 42, end = today.minusDays(1))))
        whenever(serviceService.getLatestServicesByClientId(any(), any(), any(), any(), any())).thenReturn(emptyList())
        whenever(clientTaskService.getOpenTasksByClientId(any())).thenReturn(emptyList())

        assertThat(dashboardOf(1L).currentAssistancePlan?.id).isEqualTo(42)
    }

    @Test
    fun getDashboard_unknownClient_returnsNotFound() {
        whenever(clientService.getById(404L, true, emptyList())).thenReturn(null)

        assertThat(clientDashboardService.getDashboard(404L)).isEqualTo(ClientDashboardResult.NotFound)
    }

    @Test
    fun getFavorites_combinesPlanStateAndOpenTasks() {
        givenFavoriteViewer(admin = false, leadingInstitutionIds = emptyList())
        whenever(clientDashboardRepository.findFavoriteRowDtosByEmployeeId(7L)).thenReturn(
            listOf(
                ClientFavoriteRowDto(1, "Max", "Mustermann", false, 5, "Inst"),
                ClientFavoriteRowDto(2, "Mia", "Musterfrau", false, 5, "Inst")
            )
        )
        whenever(assistancePlanPreviewService.getPeriodsByClientIds(any())).thenReturn(
            listOf(
                AssistancePlanPeriodDto(10, 1, today.minusDays(30), today.plusDays(30)),
                AssistancePlanPeriodDto(11, 2, today.minusDays(90), today.minusDays(10))
            )
        )
        whenever(clientTaskService.getOpenTaskCountsByClientIds(any())).thenReturn(
            mapOf(1L to ClientTaskCountDto(1, 3, 2))
        )

        val favorites = clientDashboardService.getFavorites()

        assertThat(favorites).hasSize(2)
        assertThat(favorites[0].hasActiveAssistancePlan).isTrue()
        assertThat(favorites[0].openTaskCount).isEqualTo(3)
        assertThat(favorites[0].overdueTaskCount).isEqualTo(2)
        assertThat(favorites[1].hasActiveAssistancePlan).isFalse()
        assertThat(favorites[1].assistancePlanEnd).isEqualTo(today.minusDays(10))
        assertThat(favorites[1].openTaskCount).isZero()
    }

    @Test
    fun getFavorites_archivedClientsAreHiddenWithoutPermission() {
        givenFavoriteViewer(admin = false, leadingInstitutionIds = listOf(9L))
        whenever(clientDashboardRepository.findFavoriteRowDtosByEmployeeId(7L)).thenReturn(
            listOf(ClientFavoriteRowDto(1, "Max", "Mustermann", true, 5, "Inst"))
        )

        assertThat(clientDashboardService.getFavorites()).isEmpty()
    }

    @Test
    fun getFavorites_archivedClientStaysVisibleForTheLeaderOfItsInstitution() {
        givenFavoriteViewer(admin = false, leadingInstitutionIds = listOf(5L))
        whenever(clientDashboardRepository.findFavoriteRowDtosByEmployeeId(7L)).thenReturn(
            listOf(ClientFavoriteRowDto(1, "Max", "Mustermann", true, 5, "Inst"))
        )
        whenever(assistancePlanPreviewService.getPeriodsByClientIds(any())).thenReturn(emptyList())
        whenever(clientTaskService.getOpenTaskCountsByClientIds(any())).thenReturn(emptyMap())

        val favorites = clientDashboardService.getFavorites()

        assertThat(favorites).hasSize(1)
        assertThat(favorites.first().archived).isTrue()
    }

    private fun dashboardOf(clientId: Long): ClientDashboardResponse {
        val result = clientDashboardService.getDashboard(clientId)
        check(result is ClientDashboardResult.Success) { "expected Success but was $result" }
        return result.response
    }

    private fun givenClient() {
        whenever(clientService.getById(1L, true, emptyList())).thenReturn(clientDto())
        whenever(clientService.getById(1L, false, emptyList())).thenReturn(clientDto())
    }

    private fun givenAccess(
        admin: Boolean = false,
        leader: Boolean = false,
        affiliated: Boolean = false,
        readableInstitutionIds: List<Long> = emptyList(),
        canWriteEntries: Boolean = false
    ) {
        whenever(accessService.isAdmin()).thenReturn(admin)
        whenever(accessService.isLeader(5L)).thenReturn(leader)
        whenever(accessService.isAffiliated(5L)).thenReturn(affiliated)
        whenever(accessService.getReadRightsInstitutionIds()).thenReturn(readableInstitutionIds)
        whenever(accessService.canWriteEntries(5L)).thenReturn(canWriteEntries)
        whenever(accessService.canModifyClient(1L)).thenReturn(false)
    }

    private fun givenFavoriteViewer(admin: Boolean, leadingInstitutionIds: List<Long>) {
        whenever(accessService.isAdmin()).thenReturn(admin)
        whenever(accessService.getLeadingInstitutionIds()).thenReturn(leadingInstitutionIds)
    }

    private fun clientDto() = ClientDetailResponse(
        id = 1,
        firstName = "Max",
        lastName = "Mustermann",
        phoneNumber = "",
        email = "",
        archived = false,
        institution = InstitutionResponse(id = 5, name = "Inst"),
        categoryTemplateId = 0,
        categoryTemplateTitle = ""
    )

    private fun runningPlan() = previewDto(id = 1, start = today.minusDays(10), end = today.plusDays(10))

    private fun endedPlan() = previewDto(id = 2, start = today.minusDays(400), end = today.minusDays(100))

    private fun futurePlan() = previewDto(id = 3, start = today.plusDays(30), end = today.plusDays(200))

    private fun previewDto(id: Long, start: LocalDate, end: LocalDate) = AssistancePlanPreviewResponse(
        id = id,
        clientId = 1,
        institutionId = 5,
        sponsorId = 9,
        clientFirstname = "Max",
        clientLastname = "Mustermann",
        clientArchived = false,
        institutionName = "Inst",
        sponsorName = "Sponsor",
        start = start,
        end = end,
        isActive = !start.isAfter(today) && !end.isBefore(today),
        isFavorite = false,
        hasIllegalHours = false,
        hourMode = AssistancePlanHourMode.EXACT,
        approvedHoursFrom = 0.0,
        approvedHoursTo = 0.0,
        approvedHoursPerWeek = 10.0,
        approvedHoursThisYearFrom = 0.0,
        approvedHoursThisYearTill = 0.0,
        approvedHoursThisYear = 100.0,
        executedHoursThisYear = 90.0,
        approvedHoursLeftThisYear = 10.0,
        approvedHoursThisAssistancePlanFrom = 0.0,
        approvedHoursThisAssistancePlanTill = 0.0,
        approvedHoursThisAssistancePlan = 50.0,
        executedHoursThisAssistancePlan = 45.0,
        approvedHoursLeftThisAssistancePlan = 5.0
    )

    private fun latestService() = ClientLatestServiceResponse(
        id = 1,
        start = LocalDateTime.of(2026, 3, 9, 9, 0),
        end = LocalDateTime.of(2026, 3, 9, 10, 0),
        minutes = 60,
        title = "Gespräch",
        content = "Inhalt",
        institutionId = 5,
        institutionName = "Inst",
        employeeId = 7,
        employeeFirstname = "Anna",
        employeeLastname = "Autorin",
        assistancePlanId = 1
    )

    private fun openTask() = ClientTaskResponse(
        id = 1,
        clientId = 1,
        title = "Offen",
        description = "",
        dueDate = today.plusDays(3),
        createdAt = LocalDateTime.of(2026, 3, 1, 9, 0),
        createdById = 7,
        createdByName = "Anna Autorin",
        done = false,
        overdue = false,
        completedById = null,
        completedByName = null,
        completedOn = null,
        completedAt = null,
        completionComment = null
    )

    private fun doneTask() = openTask().copy(id = 2, title = "Erledigt", done = true)
}
