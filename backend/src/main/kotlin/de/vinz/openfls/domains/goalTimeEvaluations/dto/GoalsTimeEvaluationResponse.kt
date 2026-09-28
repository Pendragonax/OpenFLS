package de.vinz.openfls.domains.goalTimeEvaluations.dto

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode

data class GoalsTimeEvaluationResponse(
    val assistancePlanId: Long,

    /**
     * Stundenmodus des Hilfeplans. Steuert im Frontend, ob die Hilfeplan-Zeile einzeln
     * (EXACT) oder als drei Korridor-Zeilen (CORRIDOR: Untergrenze/Obergrenze/Durchschnitt)
     * dargestellt wird.
     */
    val hourMode: AssistancePlanHourMode,

    val executedHours: List<Double>,
    val summedExecutedHours: List<Double>,
    val approvedHours: List<Double>,
    val summedApprovedHours: List<Double>,
    val approvedHoursLeft: List<Double>,
    val summedApprovedHoursLeft: List<Double>,
    val goalTimeEvaluations: List<GoalTimeEvaluationResponse>,

    /**
     * Nur bei [hourMode] == CORRIDOR gefüllt: genau drei Einträge für den Hilfeplan
     * (Untergrenze, Obergrenze, Durchschnitt). executedHours/summedExecutedHours sind in
     * allen drei Einträgen identisch (reale geleistete Stunden des Hilfeplans); die
     * genehmigten Stunden unterscheiden sich je nach Korridorgrenze.
     */
    val corridorAssistancePlanEvaluations: List<GoalTimeEvaluationResponse> = emptyList()
)
