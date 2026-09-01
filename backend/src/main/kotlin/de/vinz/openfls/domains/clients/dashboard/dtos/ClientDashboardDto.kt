package de.vinz.openfls.domains.clients.dashboard.dtos

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanPreviewDto
import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskDto
import de.vinz.openfls.domains.services.dtos.ClientLatestServiceDto

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
    val latestServices: List<ClientLatestServiceDto>,

    val tasks: List<ClientTaskDto>,
    val openTaskCount: Int
)
