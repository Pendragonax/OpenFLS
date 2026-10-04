package de.vinz.openfls.domains.goals.dto

import de.vinz.openfls.domains.goals.entity.Goal

/**
 * Schlanke Standard-Variante ohne die Zielstunden-Relation. Für den Anwendungsfall
 * „Zielstunden mitlesen" siehe [GoalWithHoursResponse].
 */
data class GoalResponse(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val assistancePlanId: Long = 0,
    val institutionId: Long? = null
) {
    companion object {
        fun from(goal: Goal): GoalResponse {
            return GoalResponse(
                id = goal.id,
                title = goal.title,
                description = goal.description,
                assistancePlanId = goal.assistancePlan?.id ?: 0,
                institutionId = goal.institution?.id
            )
        }
    }
}
