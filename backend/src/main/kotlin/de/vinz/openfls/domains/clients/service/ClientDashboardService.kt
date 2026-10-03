package de.vinz.openfls.domains.clients.service

import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanPreviewResponse
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanPreviewService
import de.vinz.openfls.domains.clientTasks.service.ClientTaskService
import de.vinz.openfls.domains.clients.dto.ClientDashboardAccess
import de.vinz.openfls.domains.clients.dto.ClientDashboardResponse
import de.vinz.openfls.domains.clients.dto.ClientDashboardResult
import de.vinz.openfls.domains.clients.dto.ClientFavoriteResponse
import de.vinz.openfls.domains.clients.repository.ClientDashboardRepository
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.services.service.ServiceService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate

/**
 * Aggregates the client dashboard from the read models of the involved domains.
 * The service never queries foreign persistence directly; it only orchestrates
 * the responses the owning domains provide and decides which section the requesting
 * employee may see.
 */
@Service
class ClientDashboardService(
    private val clientService: ClientService,
    private val clientDashboardRepository: ClientDashboardRepository,
    private val accessService: AccessService,
    private val assistancePlanPreviewService: AssistancePlanPreviewService,
    private val serviceService: ServiceService,
    private val clientTaskService: ClientTaskService,
    private val clock: Clock
) {

    companion object {
        const val LATEST_SERVICE_COUNT = 5
    }

    @Transactional(readOnly = true)
    fun getDashboard(clientId: Long): ClientDashboardResult {
        val institutionId = clientService.getById(
            id = clientId,
            includeArchived = true,
            leadingInstitutionIds = emptyList()
        )?.institution?.id ?: return ClientDashboardResult.NotFound

        val isAdmin = accessService.isAdmin()
        val isLeader = accessService.isLeader(institutionId)
        val readableInstitutionIds = accessService.getReadRightsInstitutionIds()
        val canReadDocumentation = isAdmin || isLeader || accessService.isAffiliated(institutionId) ||
            readableInstitutionIds.contains(institutionId)

        val dashboard = buildDashboard(
            clientId = clientId,
            employeeId = accessService.getId(),
            isAdmin = isAdmin,
            includeArchived = isAdmin || isLeader,
            canReadDocumentation = canReadDocumentation,
            canWriteEntries = accessService.canWriteEntries(institutionId),
            canModifyClient = accessService.canModifyClient(clientId),
            readableInstitutionIds = readableInstitutionIds
        ) ?: return ClientDashboardResult.NotFound

        return ClientDashboardResult.Success(dashboard)
    }

    /**
     * The favourite clients that open the home view, enriched with the information
     * needed to decide where to look first. Archived clients stay visible only for
     * admins and for the leaders of the client's own institution.
     */
    @Transactional(readOnly = true)
    fun getFavorites(): List<ClientFavoriteResponse> {
        val isAdmin = accessService.isAdmin()
        val leadingInstitutionIds = accessService.getLeadingInstitutionIds()
        val rows = clientDashboardRepository.findFavoriteRowDtosByEmployeeId(accessService.getId())
            .filter { !it.archived || isAdmin || leadingInstitutionIds.contains(it.institutionId) }
        if (rows.isEmpty()) {
            return emptyList()
        }

        val clientIds = rows.map { it.id }
        val today = LocalDate.now(clock)
        val periodsByClientId = assistancePlanPreviewService.getPeriodsByClientIds(clientIds)
            .groupBy { it.clientId }
        val taskCountsByClientId = clientTaskService.getOpenTaskCountsByClientIds(clientIds)

        return rows.map { row ->
            val periods = periodsByClientId[row.id].orEmpty()
            val taskCounts = taskCountsByClientId[row.id]

            ClientFavoriteResponse(
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

    private fun buildDashboard(
        clientId: Long,
        employeeId: Long,
        isAdmin: Boolean,
        includeArchived: Boolean,
        canReadDocumentation: Boolean,
        canWriteEntries: Boolean,
        canModifyClient: Boolean,
        readableInstitutionIds: List<Long>
    ): ClientDashboardResponse? {
        val client = clientService.getById(
            id = clientId,
            includeArchived = includeArchived,
            leadingInstitutionIds = emptyList()
        ) ?: return null

        val today = LocalDate.now(clock)
        val assistancePlans = if (canReadDocumentation) {
            assistancePlanPreviewService.getPreviewsByClientIdAndEmployeeId(
                clientId = clientId,
                employeeId = employeeId,
                includeArchived = includeArchived
            )
        } else {
            emptyList()
        }

        val latestServices = if (canReadDocumentation) {
            serviceService.getLatestServicesByClientId(
                clientId = clientId,
                employeeId = employeeId,
                readableInstitutionIds = readableInstitutionIds,
                isAdmin = isAdmin,
                limit = LATEST_SERVICE_COUNT
            )
        } else {
            emptyList()
        }

        val tasks = clientTaskService.getOpenTasksByClientId(clientId).orEmpty()

        return ClientDashboardResponse(
            clientId = client.id,
            firstName = client.firstName,
            lastName = client.lastName,
            archived = client.archived,
            institutionId = client.institution.id,
            institutionName = client.institution.name,
            favorite = clientDashboardRepository.findFavoriteClientIdsByEmployeeId(employeeId).contains(clientId),
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
     * The running plan wins; if none is running, the plan that ended last is shown
     * so the dashboard never looks empty for a client with a documented history.
     */
    private fun selectCurrentAssistancePlan(
        previews: List<AssistancePlanPreviewResponse>,
        today: LocalDate
    ): AssistancePlanPreviewResponse? {
        return previews
            .filter { !it.start.isAfter(today) && !it.end.isBefore(today) }
            .maxByOrNull { it.end }
            ?: previews.maxByOrNull { it.end }
    }

    private fun accessOf(granted: Boolean): ClientDashboardAccess {
        return if (granted) ClientDashboardAccess.GRANTED else ClientDashboardAccess.DENIED
    }
}
