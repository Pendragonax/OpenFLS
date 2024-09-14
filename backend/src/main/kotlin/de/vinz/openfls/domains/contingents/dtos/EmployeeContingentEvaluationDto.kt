package de.vinz.openfls.domains.contingents.dtos

data class EmployeeContingentEvaluationDto(
        val employeeId: Long,
        val lastname: String,
        val firstname: String,
        val contingentHours: List<Double>,
        val executedHours: List<Double>,
        val executedPercent: List<Double>,
        val summedExecutedPercent: List<Double>,
        val missingHours: List<Double>)