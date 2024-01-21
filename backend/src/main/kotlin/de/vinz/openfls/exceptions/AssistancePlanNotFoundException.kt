package de.vinz.openfls.exceptions

class AssistancePlanNotFoundException(id: Long): Exception("Assistance plan with id $id not found") {
}