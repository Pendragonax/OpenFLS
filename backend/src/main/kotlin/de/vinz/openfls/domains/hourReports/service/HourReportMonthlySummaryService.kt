package de.vinz.openfls.domains.hourReports.service

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.hourReports.dto.HourReportMonthlySummaryResponse
import de.vinz.openfls.domains.hourReports.dto.HourReportMonthlySummaryResult
import de.vinz.openfls.domains.hourReports.repository.HourReportMonthlySummaryRepository
import de.vinz.openfls.domains.hourReports.dto.HourReportMonthlySummaryRowResponse
import de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummaryProjection
import de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummaryGoalProjection
import de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummaryHourCorridorProjection
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.common.time.DateService
import de.vinz.openfls.common.time.TimeDoubleService
import de.vinz.openfls.domains.services.service.ServiceService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class HourReportMonthlySummaryService(
        private val hourReportMonthlySummaryRepository: HourReportMonthlySummaryRepository,
        private val serviceService: ServiceService,
        private val accessService: AccessService
) {
    /**
     * Summary of the approved against the executed hours in a month. An id of 0 or below means
     * "all" for institution, sponsor and hour type.
     */
    fun getMonthlySummary(
        year: Int,
        month: Int,
        institutionId: Long,
        sponsorId: Long,
        hourTypeId: Long
    ): HourReportMonthlySummaryResult {
        if (!accessService.canReadEntries(institutionId)) {
            return HourReportMonthlySummaryResult.Forbidden
        }
        if (year < 0 || month <= 0 || month > 12) {
            return HourReportMonthlySummaryResult.InvalidPeriod
        }
        if (sponsorId <= 0 && institutionId <= 0 && hourTypeId > 0 && !accessService.isAdmin()) {
            return HourReportMonthlySummaryResult.Forbidden
        }

        val start = LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        val assistancePlans = when {
            sponsorId > 0 && hourTypeId > 0 && institutionId <= 0 ->
                hourReportMonthlySummaryRepository.findSummaryProjectionsBySponsorIdAndPeriod(sponsorId, start, end)

            sponsorId > 0 ->
                hourReportMonthlySummaryRepository
                    .findSummaryProjectionsByInstitutionIdAndSponsorIdAndPeriod(institutionId, sponsorId, start, end)

            institutionId > 0 ->
                hourReportMonthlySummaryRepository.findSummaryProjectionsByInstitutionIdAndPeriod(institutionId, start, end)

            else ->
                hourReportMonthlySummaryRepository.findSummaryProjectionsByPeriod(start, end)
        }
        val rows = if (hourTypeId > 0 || sponsorId > 0) {
            getRowsByHourTypeIdInMonth(year, month, assistancePlans, hourTypeId)
        } else {
            getRowsInMonth(year, month, assistancePlans)
        }

        return HourReportMonthlySummaryResult.Success(createSummaryResponse(year, month, rows))
    }

    private fun createSummaryResponse(
            year: Int,
            month: Int,
            rows: List<HourReportMonthlySummaryRowResponse>): HourReportMonthlySummaryResponse {
        val approvedHours =
                if (rows.isNotEmpty())
                    rows.map { it.approvedHours }.reduce { acc, d -> TimeDoubleService.sumTimeDoubles(acc, d) }
                else
                    0.0
        val executedHours =
                if (rows.isNotEmpty())
                    rows.map { it.executedHours }.reduce { acc, d -> TimeDoubleService.sumTimeDoubles(acc, d) }
                else
                    0.0
        val executedPercent =
                if (approvedHours > 0)
                    TimeDoubleService.roundDoubleToTwoDigits(executedHours * 100 / approvedHours)
                else
                    0.0
        val missingHours = TimeDoubleService.diffTimeDoubles(approvedHours, executedHours)

        return HourReportMonthlySummaryResponse(
                year = year,
                month = month,
                approvedHours = approvedHours,
                executedHours = executedHours,
                executedPercent = executedPercent,
                missingHours = missingHours,
                rows = rows.sortedBy { it.clientLastName })
    }

    private fun getRowsByHourTypeIdInMonth(year: Int,
                                       month: Int,
                                       assistancePlans: List<HourReportMonthlySummaryProjection>,
                                       hourTypeId: Long): List<HourReportMonthlySummaryRowResponse> {
        return assistancePlans
                .map { getRowsByHourTypeIdInMonth(year, month, it, hourTypeId) }
                .filter { it.approvedHours > 0 }
    }

    private fun getRowsByHourTypeIdInMonth(year: Int,
                                       month: Int,
                                       assistancePlan: HourReportMonthlySummaryProjection,
                                       hourTypeId: Long): HourReportMonthlySummaryRowResponse {
        val approvedHours =
                if (isCorridor(assistancePlan)) {
                    getApprovedCorridorHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId)
                } else if (existsAssistancePlanHours(assistancePlan)) {
                    getApprovedAssistancePlanHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId)
                } else {
                    getApprovedGoalHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId)
                }
        val executedHours = getExecutedHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId)
        val approvedRange = getApprovedRangeInMonth(year, month, assistancePlan, hourTypeId)
        val executedPercent = calculateExecutedPercent(executedHours, approvedRange.first, approvedRange.second, approvedHours)
        val missingHours = calculateMissingHours(executedHours, approvedRange.first, approvedRange.second)

        return getRowsInMonth(
                year,
                month,
                assistancePlan,
                approvedHours,
                executedHours,
                executedPercent,
                missingHours)
    }

    private fun getRowsInMonth(year: Int,
                           month: Int,
                           assistancePlans: List<HourReportMonthlySummaryProjection>): List<HourReportMonthlySummaryRowResponse> {
        return assistancePlans
                .map { getRowsInMonth(year, month, it) }
                .filter { it.approvedHours > 0 }
    }

    private fun getRowsInMonth(year: Int,
                           month: Int,
                           assistancePlan: HourReportMonthlySummaryProjection): HourReportMonthlySummaryRowResponse {
        val approvedHours =
                if (isCorridor(assistancePlan)) {
                    getApprovedCorridorHoursInMonth(year, month, assistancePlan)
                } else if (existsAssistancePlanHours(assistancePlan)) {
                    getApprovedAssistancePlanHoursInMonth(year, month, assistancePlan)
                } else {
                    getApprovedGoalHoursInMonth(year, month, assistancePlan)
                }
        val executedHours = getExecutedHoursInMonth(year, month, assistancePlan)
        val approvedRange = getApprovedRangeInMonth(year, month, assistancePlan)
        val executedPercent = calculateExecutedPercent(executedHours, approvedRange.first, approvedRange.second, approvedHours)
        val missingHours = calculateMissingHours(executedHours, approvedRange.first, approvedRange.second)

        return getRowsInMonth(
                year,
                month,
                assistancePlan,
                approvedHours,
                executedHours,
                executedPercent,
                missingHours)
    }

    private fun getRowsInMonth(year: Int,
                           month: Int,
                           assistancePlan: HourReportMonthlySummaryProjection,
                           approvedHours: Double,
                           executedHours: Double,
                           executedPercent: Double,
                           missingHours: Double): HourReportMonthlySummaryRowResponse {
        return HourReportMonthlySummaryRowResponse(
                assistancePlanId = assistancePlan.id,
                start = assistancePlan.start,
                end = assistancePlan.end,
                clientFirstName = assistancePlan.client.firstName,
                clientLastName = assistancePlan.client.lastName,
                hourMode = assistancePlan.hourMode,
                year = year,
                month = month,
                approvedHours = approvedHours,
                executedHours = executedHours,
                executedPercent = executedPercent,
                missingHours = missingHours)
    }

    private fun getApprovedAssistancePlanHoursInMonth(year: Int,
                                              month: Int,
                                              assistancePlan: HourReportMonthlySummaryProjection): Double {
        if (isCorridor(assistancePlan)) {
            return getApprovedCorridorHoursInMonth(year, month, assistancePlan)
        }
        val dailyMinutes = assistancePlan.hours.sumOf { it.weeklyMinutes } / 7.0
        val days = countMatchingDaysInMonth(year, month, assistancePlan)
        return TimeDoubleService.convertDoubleToTimeDouble(
                (dailyMinutes * days) / 60.0)
    }

    private fun getApprovedAssistancePlanHoursByHourTypeIdInMonth(year: Int,
                                                          month: Int,
                                                          assistancePlan: HourReportMonthlySummaryProjection,
                                                          hourTypeId: Long): Double {
        if (isCorridor(assistancePlan)) {
            return getApprovedCorridorHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId)
        }
        val hours = assistancePlan.hours.filter { it.hourType.id == hourTypeId }
        val dailyMinutes = hours.sumOf { it.weeklyMinutes } / 7.0
        val days = countMatchingDaysInMonth(year, month, assistancePlan)
        return TimeDoubleService.convertDoubleToTimeDouble((dailyMinutes * days) / 60.0)
    }

    private fun getApprovedGoalHoursByHourTypeIdInMonth(year: Int,
                                                month: Int,
                                                assistancePlan: HourReportMonthlySummaryProjection,
                                                hourTypeId: Long): Double {
        if (assistancePlan.goals.isEmpty()) {
            return 0.0
        }

        val days = countMatchingDaysInMonth(year, month, assistancePlan)
        val approvedHours = sumGoalsHoursByHourTypeId(assistancePlan.goals, days, hourTypeId)
        return TimeDoubleService.convertDoubleToTimeDouble(approvedHours)
    }

    private fun getApprovedGoalHoursInMonth(year: Int,
                                    month: Int,
                                    assistancePlan: HourReportMonthlySummaryProjection): Double {
        if (assistancePlan.goals.isEmpty()) {
            return 0.0
        }

        val days = countMatchingDaysInMonth(year, month, assistancePlan)
        val approvedHours = sumGoalsHours(assistancePlan.goals, days)
        return TimeDoubleService.convertDoubleToTimeDouble(approvedHours)
    }

    private fun getExecutedHoursInMonth(year: Int,
                                month: Int,
                                assistancePlan: HourReportMonthlySummaryProjection): Double {
        val services = serviceService.getServicesByAssistancePlanIdAndYearAndMonth(
                assistancePlanId = assistancePlan.id,
                year = year,
                month = month)
        val hours = services.sumOf { it.minutes.toDouble() } / 60

        return TimeDoubleService.convertDoubleToTimeDouble(hours)
    }

    private fun getExecutedHoursByHourTypeIdInMonth(year: Int,
                                            month: Int,
                                            assistancePlan: HourReportMonthlySummaryProjection,
                                            hourTypeId: Long): Double {
        val services = serviceService.getServicesByAssistancePlanIdAndHourTypeIdAndYearAndMonth(
                assistancePlanId = assistancePlan.id,
                hourTypeId = hourTypeId,
                year = year,
                month = month)
        val hours = services.sumOf { it.minutes.toDouble() } / 60

        return TimeDoubleService.convertDoubleToTimeDouble(hours)
    }

    private fun getApprovedCorridorHoursInMonth(
        year: Int,
        month: Int,
        assistancePlan: HourReportMonthlySummaryProjection
    ): Double {
        val corridor = assistancePlan.hourCorridor ?: return 0.0
        val days = countMatchingDaysInMonth(year, month, assistancePlan)
        return corridorApprovedHours(corridor, days)
    }

    private fun getApprovedCorridorHoursByHourTypeIdInMonth(
        year: Int,
        month: Int,
        assistancePlan: HourReportMonthlySummaryProjection,
        hourTypeId: Long
    ): Double {
        val corridor = assistancePlan.hourCorridor ?: return 0.0
        if ((corridor.hourType?.id ?: 0) != hourTypeId) {
            return 0.0
        }
        val days = countMatchingDaysInMonth(year, month, assistancePlan)
        return corridorApprovedHours(corridor, days)
    }

    private fun getApprovedRangeInMonth(
        year: Int,
        month: Int,
        assistancePlan: HourReportMonthlySummaryProjection
    ): Pair<Double, Double> {
        return if (isCorridor(assistancePlan)) {
            val corridor = assistancePlan.hourCorridor
            val days = countMatchingDaysInMonth(year, month, assistancePlan)
            val from = corridor?.let { corridorApprovedHoursForDays(days, it.weeklyMinutesFrom) } ?: 0.0
            val till = corridor?.let { corridorApprovedHoursForDays(days, it.weeklyMinutesTill) } ?: from
            from to till
        } else {
            val approvedHours = if (existsAssistancePlanHours(assistancePlan)) {
                getApprovedAssistancePlanHoursInMonth(year, month, assistancePlan)
            } else {
                getApprovedGoalHoursInMonth(year, month, assistancePlan)
            }
            approvedHours to approvedHours
        }
    }

    private fun getApprovedRangeInMonth(
        year: Int,
        month: Int,
        assistancePlan: HourReportMonthlySummaryProjection,
        hourTypeId: Long
    ): Pair<Double, Double> {
        return if (isCorridor(assistancePlan)) {
            val corridor = assistancePlan.hourCorridor
            if (corridor?.hourType?.id != hourTypeId) {
                0.0 to 0.0
            } else {
                val days = countMatchingDaysInMonth(year, month, assistancePlan)
                val from = corridorApprovedHoursForDays(days, corridor.weeklyMinutesFrom)
                val till = corridorApprovedHoursForDays(days, corridor.weeklyMinutesTill)
                from to till
            }
        } else {
            val approvedHours = if (existsAssistancePlanHours(assistancePlan)) {
                getApprovedAssistancePlanHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId)
            } else {
                getApprovedGoalHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId)
            }
            approvedHours to approvedHours
        }
    }

    private fun calculateExecutedPercent(
        executedHours: Double,
        approvedHoursFrom: Double,
        approvedHoursTo: Double,
        approvedHours: Double
    ): Double {
        if (approvedHoursTo <= 0.0) {
            return 0.0
        }

        return when {
            executedHours < approvedHoursFrom -> TimeDoubleService.roundDoubleToTwoDigits(executedHours * 100 / approvedHoursFrom)
            executedHours > approvedHoursTo -> TimeDoubleService.roundDoubleToTwoDigits(executedHours * 100 / approvedHoursTo)
            else -> TimeDoubleService.roundDoubleToTwoDigits(executedHours * 100 / approvedHours)
        }
    }

    private fun calculateMissingHours(executedHours: Double, approvedHoursFrom: Double, approvedHoursTo: Double): Double {
        return when {
            executedHours < approvedHoursFrom -> TimeDoubleService.diffTimeDoubles(approvedHoursFrom, executedHours)
            executedHours > approvedHoursTo -> TimeDoubleService.diffTimeDoubles(approvedHoursTo, executedHours)
            else -> 0.0
        }
    }

    private fun corridorApprovedHours(corridor: HourReportMonthlySummaryHourCorridorProjection, days: Int): Double {
        val weeklyMinutesMean = (corridor.weeklyMinutesFrom + corridor.weeklyMinutesTill) / 2.0
        return TimeDoubleService.convertDoubleToTimeDouble((weeklyMinutesMean / 7.0) * days / 60.0)
    }

    private fun corridorApprovedHoursForDays(days: Int, weeklyMinutes: Int): Double {
        return TimeDoubleService.convertDoubleToTimeDouble((weeklyMinutes / 7.0) * days / 60.0)
    }

    private fun isCorridor(assistancePlan: HourReportMonthlySummaryProjection): Boolean {
        return assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR
    }

    private fun countMatchingDaysInMonth(year: Int, month: Int, assistancePlan: HourReportMonthlySummaryProjection): Int {
        if (!isInYearMonth(year, month, assistancePlan)) {
            return 0
        }

        return DateService.countDaysOfMonthAndYearBetweenStartAndEnd(
                year,
                month,
                assistancePlan.start,
                assistancePlan.end)
    }

    private fun sumGoalsHours(goals: List<HourReportMonthlySummaryGoalProjection>, days: Int): Double {
        return goals.sumOf { sumGoalHours(it, days) }
    }

    private fun sumGoalsHoursByHourTypeId(goals: List<HourReportMonthlySummaryGoalProjection>, days: Int, hourTypeId: Long): Double {
        return goals.sumOf { sumGoalHoursByHourTypeId(it, days, hourTypeId) }
    }

    private fun sumGoalHours(goal: HourReportMonthlySummaryGoalProjection, days: Int): Double {
        if (goal.hours.isEmpty()) {
            return 0.0
        }

        return goal.hours.sumOf { hour -> (hour.weeklyMinutes / 7.0) * days / 60.0 }
    }

    private fun sumGoalHoursByHourTypeId(goal: HourReportMonthlySummaryGoalProjection, days: Int, hourTypeId: Long): Double {
        if (goal.hours.isEmpty()) {
            return 0.0
        }

        val hours = goal.hours.filter { it.hourType.id == hourTypeId }
        return hours.sumOf { hour -> (hour.weeklyMinutes / 7.0) * days / 60.0 }
    }

    private fun isInYearMonth(year: Int, month: Int, assistancePlan: HourReportMonthlySummaryProjection): Boolean {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return assistancePlan.start <= end && assistancePlan.end >= start
    }

    private fun existsAssistancePlanHours(assistancePlan: HourReportMonthlySummaryProjection): Boolean {
        return assistancePlan.hours.isNotEmpty()
    }
}
