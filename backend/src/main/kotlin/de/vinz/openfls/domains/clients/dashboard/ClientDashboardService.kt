package de.vinz.openfls.domains.clients.dashboard

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanPreviewDto
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanPreviewService
import de.vinz.openfls.domains.clientTasks.ClientTaskService
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.clients.dashboard.dtos.ClientDashboardAccess
import de.vinz.openfls.domains.clients.dashboard.dtos.ClientDashboardDto
import de.vinz.openfls.domains.clients.dashboard.dtos.ClientFavoriteDto
import de.vinz.openfls.domains.services.services.ServiceService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate

/**
 * Aggregates the client dashboard from the read models of the involved domains.
 * The service never queries foreign persistence directly; it only orchestrates
 * the Dtos the owning domains provide and decides which section the requesting
 * employee may see.
 */
@Service
class ClientDashboardService(
    private val clientService: ClientService,
    private val assistancePlanPreviewService: AssistancePlanPreviewService,
    private val serviceService: ServiceService,
    private val clientTaskService: ClientTaskService,
    private val clock: Clock
) {

    companion object {
        const val LATEST_SERVICE_COUNT = 5
    }

    /**
     * @param employeeId requesting employee
     * @param includeArchived whether archived clients and plans stay visible
     * @param canReadDocumentation whether the employee may read assistance plans and
     *        entries of the client's institution
     */
    @Transactional(readOnly = true)
    fun getDashboard(
        clientId: Long,
        employeeId: Long,
        isAdmin: Boolean,
        includeArchived: Boolean,
        canReadDocumentation: Boolean,
        canWriteEntries: Boolean,
        canModifyClient: Boolean,
        readableInstitutionIds: List<Long>
    ): ClientDashboardDto? {
        val client = clientService.getDtoById(
            id = clientId,
            includeArchived = includeArchived,
            leadingInstitutionIds = emptyList()
        ) ?: return null

        val today = LocalDate.now(clock)
        val assistancePlans = if (canReadDocumentation) {
            assistancePlanPreviewService.getPreviewDtosByClientId(
                clientId = clientId,
                employeeId = employeeId,
                includeArchived = includeArchived
            )
        } else {
            emptyList()
        }

        val latestServices = if (canReadDocumentation) {
            serviceService.getLatestDtosByClientId(
                clientId = clientId,
                employeeId = employeeId,
                readableInstitutionIds = readableInstitutionIds,
                isAdmin = isAdmin,
                limit = LATEST_SERVICE_COUNT
            )
        } else {
            emptyList()
        }

        val tasks = clientTaskService.getDtosByClientId(clientId)

        return ClientDashboardDto(
            clientId = client.id,
            firstName = client.firstName,
            lastName = client.lastName,
            archived = client.archived,
            institutionId = client.institution.id,
            institutionName = client.institution.name,
            favorite = clientService.isFavoriteOfEmployee(clientId, employeeId),
            canModifyClient = canModifyClient,
            canWriteEntries = canWriteEntries,
            assistancePlanAccess = accessOf(canReadDocumentation),
            currentAssistancePlan = selectCurrentAssistancePlan(assistancePlans, today),
            assistancePlanCount = assistancePlans.size,
            servicesAccess = accessOf(canReadDocumentation),
            latestServices = latestServices,
            tasks = tasks,
            openTaskCount = tasks.count { !it.done }
        )
    }

    /**
     * The favourite clients that open the home view, enriched with the information
     * needed to decide where to look first. Archived clients stay visible only for
     * admins and for the leaders of the client's own institution.
     */
    @Transactional(readOnly = true)
    fun getFavorites(
        employeeId: Long,
        isAdmin: Boolean,
        leadingInstitutionIds: List<Long>
    ): List<ClientFavoriteDto> {
        val rows = clientService.getFavoriteRowDtosByEmployeeId(employeeId)
            .filter { !it.archived || isAdmin || leadingInstitutionIds.contains(it.institutionId) }
        if (rows.isEmpty()) {
            return emptyList()
        }

        val clientIds = rows.map { it.id }
        val today = LocalDate.now(clock)
        val periodsByClientId = assistancePlanPreviewService.getPeriodDtosByClientIds(clientIds)
            .groupBy { it.clientId }
        val taskCountsByClientId = clientTaskService.getOpenTaskCountsByClientIds(clientIds)

        return rows.map { row ->
            val periods = periodsByClientId[row.id].orEmpty()
            val taskCounts = taskCountsByClientId[row.id]

            ClientFavoriteDto(
                clientId = row.id,
                firstName = row.firstName,
                lastName = row.lastName,
                archived = row.archived,
                institutionId = row.institutionId,
                institutionName = row.institutionName,
                hasActiveAssistancePlan = periods.any { !it.start.isAfter(today) && !it.end.isBefore(today) },
                assistancePlanEnd = periods.maxByOrNull { it.end }?.end,
                openTaskCount = (taskCounts?.openCount ?: 0L).toInt(),
                overdueTaskCount = (taskCounts?.overdueCount ?: 0L).toInt()
            )
        }
    }

    /**
     * The running plan wins; if none is running, the plan that ended last is shown
     * so the dashboard never looks empty for a client with a documented history.
     */
    private fun selectCurrentAssistancePlan(
        previews: List<AssistancePlanPreviewDto>,
        today: LocalDate
    ): AssistancePlanPreviewDto? {
        return previews
            .filter { !it.start.isAfter(today) && !it.end.isBefore(today) }
            .maxByOrNull { it.end }
            ?: previews.maxByOrNull { it.end }
    }

    private fun accessOf(granted: Boolean): ClientDashboardAccess {
        return if (granted) ClientDashboardAccess.GRANTED else ClientDashboardAccess.DENIED
    }
}
