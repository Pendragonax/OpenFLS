package de.vinz.openfls.domains.clients.dashboard

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanPeriodDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanPreviewDto
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanPreviewService
import de.vinz.openfls.domains.clientTasks.ClientTaskService
import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskCountDto
import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskDto
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.clients.dashboard.dtos.ClientDashboardAccess
import de.vinz.openfls.domains.clients.dtos.ClientDto
import de.vinz.openfls.domains.clients.dtos.ClientFavoriteRowDto
import de.vinz.openfls.domains.services.dtos.ClientLatestServiceDto
import de.vinz.openfls.domains.services.services.ServiceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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
    private val assistancePlanPreviewService: AssistancePlanPreviewService = mock()
    private val serviceService: ServiceService = mock()
    private val clientTaskService: ClientTaskService = mock()

    private val clientDashboardService = ClientDashboardService(
        clientService,
        assistancePlanPreviewService,
        serviceService,
        clientTaskService,
        clock
    )

    @Test
    fun getDashboard_withReadPermission_returnsRunningPlanAndEntries() {
        whenever(clientService.getDtoById(any(), any(), any())).thenReturn(clientDto())
        whenever(clientService.isFavoriteOfEmployee(any(), any())).thenReturn(true)
        whenever(assistancePlanPreviewService.getPreviewDtosByClientId(any(), any(), any()))
            .thenReturn(listOf(endedPlan(), runningPlan(), futurePlan()))
        whenever(serviceService.getLatestDtosByClientId(any(), any(), any(), any(), any()))
            .thenReturn(listOf(latestService()))
        whenever(clientTaskService.getDtosByClientId(any())).thenReturn(listOf(openTask(), doneTask()))

        val dashboard = clientDashboardService.getDashboard(
            clientId = 1,
            employeeId = 7,
            isAdmin = false,
            includeArchived = false,
            canReadDocumentation = true,
            canWriteEntries = true,
            canModifyClient = false,
            readableInstitutionIds = listOf(5)
        )

        requireNotNull(dashboard)
        assertThat(dashboard.assistancePlanAccess).isEqualTo(ClientDashboardAccess.GRANTED)
        assertThat(dashboard.servicesAccess).isEqualTo(ClientDashboardAccess.GRANTED)
        assertThat(dashboard.currentAssistancePlan?.id).isEqualTo(runningPlan().id)
        assertThat(dashboard.assistancePlanCount).isEqualTo(3)
        assertThat(dashboard.latestServices).hasSize(1)
        assertThat(dashboard.openTaskCount).isEqualTo(1)
        assertThat(dashboard.favorite).isTrue()
    }

    @Test
    fun getDashboard_withoutReadPermission_deniesPlanAndEntriesButKeepsTasks() {
        whenever(clientService.getDtoById(any(), any(), any())).thenReturn(clientDto())
        whenever(clientService.isFavoriteOfEmployee(any(), any())).thenReturn(false)
        whenever(clientTaskService.getDtosByClientId(any())).thenReturn(listOf(openTask()))

        val dashboard = clientDashboardService.getDashboard(
            clientId = 1,
            employeeId = 7,
            isAdmin = false,
            includeArchived = false,
            canReadDocumentation = false,
            canWriteEntries = false,
            canModifyClient = false,
            readableInstitutionIds = emptyList()
        )

        requireNotNull(dashboard)
        assertThat(dashboard.assistancePlanAccess).isEqualTo(ClientDashboardAccess.DENIED)
        assertThat(dashboard.servicesAccess).isEqualTo(ClientDashboardAccess.DENIED)
        assertThat(dashboard.currentAssistancePlan).isNull()
        assertThat(dashboard.assistancePlanCount).isZero()
        assertThat(dashboard.latestServices).isEmpty()
        assertThat(dashboard.tasks).hasSize(1)

        verify(assistancePlanPreviewService, never()).getPreviewDtosByClientId(any(), any(), any())
        verify(serviceService, never()).getLatestDtosByClientId(any(), any(), any(), any(), any())
    }

    @Test
    fun getDashboard_withoutRunningPlan_fallsBackToPlanThatEndedLast() {
        whenever(clientService.getDtoById(any(), any(), any())).thenReturn(clientDto())
        whenever(clientService.isFavoriteOfEmployee(any(), any())).thenReturn(false)
        whenever(assistancePlanPreviewService.getPreviewDtosByClientId(any(), any(), any()))
            .thenReturn(listOf(endedPlan(), endedPlan().copy(id = 42, end = today.minusDays(1))))
        whenever(serviceService.getLatestDtosByClientId(any(), any(), any(), any(), any())).thenReturn(emptyList())
        whenever(clientTaskService.getDtosByClientId(any())).thenReturn(emptyList())

        val dashboard = clientDashboardService.getDashboard(
            clientId = 1,
            employeeId = 7,
            isAdmin = false,
            includeArchived = false,
            canReadDocumentation = true,
            canWriteEntries = false,
            canModifyClient = false,
            readableInstitutionIds = listOf(5)
        )

        assertThat(dashboard?.currentAssistancePlan?.id).isEqualTo(42)
    }

    @Test
    fun getDashboard_unknownClient_returnsNull() {
        whenever(clientService.getDtoById(any(), any(), any())).thenReturn(null)

        val dashboard = clientDashboardService.getDashboard(
            clientId = 404,
            employeeId = 7,
            isAdmin = false,
            includeArchived = false,
            canReadDocumentation = true,
            canWriteEntries = false,
            canModifyClient = false,
            readableInstitutionIds = emptyList()
        )

        assertThat(dashboard).isNull()
    }

    @Test
    fun getFavorites_combinesPlanStateAndOpenTasks() {
        whenever(clientService.getFavoriteRowDtosByEmployeeId(eq(7L))).thenReturn(
            listOf(
                ClientFavoriteRowDto(1, "Max", "Mustermann", false, 5, "Inst"),
                ClientFavoriteRowDto(2, "Mia", "Musterfrau", false, 5, "Inst")
            )
        )
        whenever(assistancePlanPreviewService.getPeriodDtosByClientIds(any())).thenReturn(
            listOf(
                AssistancePlanPeriodDto(10, 1, today.minusDays(30), today.plusDays(30)),
                AssistancePlanPeriodDto(11, 2, today.minusDays(90), today.minusDays(10))
            )
        )
        whenever(clientTaskService.getOpenTaskCountsByClientIds(any())).thenReturn(
            mapOf(1L to ClientTaskCountDto(1, 3, 2))
        )

        val favorites = clientDashboardService.getFavorites(
            employeeId = 7,
            isAdmin = false,
            leadingInstitutionIds = emptyList()
        )

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
        whenever(clientService.getFavoriteRowDtosByEmployeeId(eq(7L))).thenReturn(
            listOf(ClientFavoriteRowDto(1, "Max", "Mustermann", true, 5, "Inst"))
        )

        val favorites = clientDashboardService.getFavorites(
            employeeId = 7,
            isAdmin = false,
            leadingInstitutionIds = listOf(9)
        )

        assertThat(favorites).isEmpty()
    }

    @Test
    fun getFavorites_archivedClientStaysVisibleForTheLeaderOfItsInstitution() {
        whenever(clientService.getFavoriteRowDtosByEmployeeId(eq(7L))).thenReturn(
            listOf(ClientFavoriteRowDto(1, "Max", "Mustermann", true, 5, "Inst"))
        )
        whenever(assistancePlanPreviewService.getPeriodDtosByClientIds(any())).thenReturn(emptyList())
        whenever(clientTaskService.getOpenTaskCountsByClientIds(any())).thenReturn(emptyMap())

        val favorites = clientDashboardService.getFavorites(
            employeeId = 7,
            isAdmin = false,
            leadingInstitutionIds = listOf(5)
        )

        assertThat(favorites).hasSize(1)
        assertThat(favorites.first().archived).isTrue()
    }

    private fun clientDto(): ClientDto = ClientDto().apply {
        id = 1
        firstName = "Max"
        lastName = "Mustermann"
        archived = false
        institution.id = 5
        institution.name = "Inst"
    }

    private fun runningPlan() = previewDto(id = 1, start = today.minusDays(10), end = today.plusDays(10))

    private fun endedPlan() = previewDto(id = 2, start = today.minusDays(400), end = today.minusDays(100))

    private fun futurePlan() = previewDto(id = 3, start = today.plusDays(30), end = today.plusDays(200))

    private fun previewDto(id: Long, start: LocalDate, end: LocalDate) = AssistancePlanPreviewDto(
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

    private fun latestService() = ClientLatestServiceDto(
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

    private fun openTask() = ClientTaskDto(
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
