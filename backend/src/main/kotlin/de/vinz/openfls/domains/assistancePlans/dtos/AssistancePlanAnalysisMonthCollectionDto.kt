package de.vinz.openfls.domains.assistancePlans.dtos

data class AssistancePlanAnalysisMonthCollectionDto(
        val year: Int,
        val month: Int,
        val approvedHours: Double,
        val executedHours: Double,
        val executedPercent: Double,
        val missingHours: Double,
        val assistancePlanAnalysis: List<AssistancePlanAnalysisMonthDto>)