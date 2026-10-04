package de.vinz.openfls.domains.services.dto

import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Die dokumentierende Person wird nicht übergeben, sie ist immer die angemeldete Person.
 */
data class ServiceCreateRequest(
    val start: LocalDateTime = LocalDateTime.now(),
    val end: LocalDateTime = LocalDateTime.now(),

    @field:Size(max = 64)
    val title: String = "",

    @field:Size(max = 1024)
    val content: String = "",

    val unfinished: Boolean = false,
    val groupService: Boolean = false,
    val clientId: Long = 0,
    val institutionId: Long = 0,
    val assistancePlanId: Long = 0,
    val hourTypeId: Long = 0,
    val goals: List<IdReferenceRequest> = emptyList(),
    val categorys: List<IdReferenceRequest> = emptyList()
)
