package de.vinz.openfls.domains.contingents.dtos

import de.vinz.openfls.domains.contingents.Contingent
import java.time.LocalDate

data class ContingentResponse(
    val id: Long = 0,
    val start: LocalDate = LocalDate.now(),
    val end: LocalDate? = null,
    val weeklyServiceHours: Double = 0.0,
    val employeeId: Long = 0,
    val institutionId: Long = 0
) {
    companion object {
        fun from(contingent: Contingent): ContingentResponse {
            return ContingentResponse(
                id = contingent.id,
                start = contingent.start,
                end = contingent.end,
                weeklyServiceHours = contingent.weeklyServiceHours,
                employeeId = contingent.employee?.id ?: 0,
                institutionId = contingent.institution?.id ?: 0
            )
        }
    }
}
