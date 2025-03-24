package de.vinz.openfls.domains.assistancePlans.projections

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.hourTypes.projections.HourTypeSoloProjection

interface AssistancePlanHourProjection {
    val id: Long
    val weeklyHours: Double
    val hourType: HourTypeSoloProjection
    val assistancePlan: AssistancePlanSoloProjection

    companion object {
        fun from(value: AssistancePlanHour): AssistancePlanHourProjection {
            return object : AssistancePlanHourProjection {
                override val id = value.id
                override val weeklyHours = value.weeklyHours
                override val hourType = HourTypeSoloProjection.from(value.hourType)
                override val assistancePlan = AssistancePlanSoloProjection.from(value.assistancePlan)
            }
        }
    }
}