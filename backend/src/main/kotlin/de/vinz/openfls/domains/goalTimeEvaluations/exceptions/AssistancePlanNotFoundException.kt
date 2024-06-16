package de.vinz.openfls.domains.goalTimeEvaluations.exceptions

class AssistancePlanNotFoundException(id: Long): Exception("Assistance plan with id $id not found") {
}