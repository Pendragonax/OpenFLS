package de.vinz.openfls.domains.goalTimeEvaluations.exceptions

class NoGoalFoundWithHourTypeException(hourTypeId: Long) : Exception("No matching goal found by the given hourType [id = $hourTypeId]") {
}
