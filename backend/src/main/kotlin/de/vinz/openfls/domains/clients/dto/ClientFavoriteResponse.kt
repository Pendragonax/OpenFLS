package de.vinz.openfls.domains.clients.dto

import java.time.LocalDate

/**
 * Entry of the favourite client list that opens the home view.
 */
data class ClientFavoriteResponse(
    val clientId: Long,
    val firstName: String,
    val lastName: String,
    val archived: Boolean,
    val institutionId: Long,
    val institutionName: String,
    val hasActiveAssistancePlan: Boolean,
    val assistancePlanEnd: LocalDate?,
    val openTaskCount: Int,
    val overdueTaskCount: Int
)
