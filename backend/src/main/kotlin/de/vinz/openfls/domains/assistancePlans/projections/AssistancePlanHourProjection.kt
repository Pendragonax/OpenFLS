package de.vinz.openfls.domains.assistancePlans.projections

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.hourTypes.projections.HourTypeSoloProjection

interface AssistancePlanHourProjection {
    val id: Long
    val weeklyMinutes: Int
    val hourType: HourTypeSoloProjection
    val assistancePlan: AssistancePlanSoloProjection

    companion object {
        fun from(value: AssistancePlanHour): AssistancePlanHourProjection {
            return object : AssistancePlanHourProjection {
                override val id = value.id
                override val weeklyMinutes = value.weeklyMinutes
                override val hourType = HourTypeSoloProjection.from(value.hourType)
                override val assistancePlan = AssistancePlanSoloProjection.from(value.assistancePlan)
            }
        }
    }
}
