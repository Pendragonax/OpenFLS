package de.vinz.openfls.domains.goalTimeEvaluations.dtos

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode

class GoalsTimeEvaluationDto {

    var assistancePlanId: Long = 0

    /**
     * Stundenmodus des Hilfeplans. Steuert im Frontend, ob die Hilfeplan-Zeile einzeln
     * (EXACT) oder als drei Korridor-Zeilen (CORRIDOR: Untergrenze/Obergrenze/Durchschnitt)
     * dargestellt wird.
     */
    var hourMode: AssistancePlanHourMode = AssistancePlanHourMode.EXACT

    var executedHours: List<Double> = listOf()

    var summedExecutedHours: List<Double> = listOf()

    var approvedHours: List<Double> = listOf()

    var summedApprovedHours: List<Double> = listOf()

    var approvedHoursLeft: List<Double> = listOf()

    var summedApprovedHoursLeft: List<Double> = listOf()

    var goalTimeEvaluations: MutableList<GoalTimeEvaluationDto> = mutableListOf()

    /**
     * Nur bei [hourMode] == CORRIDOR gefüllt: genau drei Einträge für den Hilfeplan
     * (Untergrenze, Obergrenze, Durchschnitt). executedHours/summedExecutedHours sind in
     * allen drei Einträgen identisch (reale geleistete Stunden des Hilfeplans); die
     * genehmigten Stunden unterscheiden sich je nach Korridorgrenze.
     */
    var corridorAssistancePlanEvaluations: MutableList<GoalTimeEvaluationDto> = mutableListOf()
}
