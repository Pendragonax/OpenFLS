package de.vinz.openfls.domains.assistancePlans.projections

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import java.time.LocalDate

interface AssistancePlanSoloProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate

    companion object {
        fun from(value: AssistancePlan?): AssistancePlanSoloProjection {
            return object : AssistancePlanSoloProjection {
                override val id = value?.id ?: 0
                override val start = value?.start ?: LocalDate.now()
                override val end = value?.end ?: LocalDate.now()
            }
        }
    }
}