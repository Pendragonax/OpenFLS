package de.vinz.openfls.domains.clients.dashboard.dtos

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanPreviewDto
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskResponse
import de.vinz.openfls.domains.services.dto.ClientLatestServiceResponse

/**
 * Everything the client dashboard shows on one page. Each section carries its own
 * access state so that missing permissions are visible instead of looking like
 * missing data.
 */
data class ClientDashboardDto(
    val clientId: Long,
    val firstName: String,
    val lastName: String,
    val archived: Boolean,
    val institutionId: Long,
    val institutionName: String,
    val favorite: Boolean,
    val canModifyClient: Boolean,
    val canWriteEntries: Boolean,

    val assistancePlanAccess: ClientDashboardAccess,
    val currentAssistancePlan: AssistancePlanPreviewDto?,
    val assistancePlanCount: Int,

    val servicesAccess: ClientDashboardAccess,
    val latestServices: List<ClientLatestServiceResponse>,

    val tasks: List<ClientTaskResponse>,
    val openTaskCount: Int
)
