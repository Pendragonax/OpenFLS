package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.dtos.ApprovedHoursLeftResponseDTO
import de.vinz.openfls.domains.assistancePlans.dtos.ApprovedHoursLeftResponseDTO.HourTypeEvaluationDTO
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.services.services.ServiceService
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class AssistancePlanEvaluationLeftService(
    private val assistancePlanService: AssistancePlanService,
    private val serviceService: ServiceService
) {

    fun createAssistancePlanHourTypeAnalysis(date: LocalDate, assistancePlanId: Long): ApprovedHoursLeftResponseDTO {
        val assistancePlan =
            assistancePlanService.getById(assistancePlanId) ?: throw IllegalArgumentException("No such assistance-plan")
        val hourTypes = getDistinctHourTypesIn(assistancePlan)
        val approvedRangeMinutes = getApprovedRangeMinutes(assistancePlan)

        val hourTypeEvaluation = hourTypes.map { hourType ->
            val approvedMinutesLeftInWeek = getApprovedMinutesLeftIn(date, assistancePlan, hourType.id)
            val approvedMinutesLeftInMonth =
                getApprovedMinutesLeftIn(date.year, date.monthValue, assistancePlan, hourType.id)
            val approvedMinutesLeftInYear = getApprovedMinutesLeftIn(date.year, assistancePlan, hourType.id)
            val approvedMinutesLeftComplete = getApprovedMinutesLeftIn(assistancePlan, hourType.id)

            HourTypeEvaluationDTO(
                hourTypeName = hourType.title,
                leftThisWeek = TimeDoubleService.convertDoubleToTimeDouble(approvedMinutesLeftInWeek / 60.0),
                leftThisMonth = TimeDoubleService.convertDoubleToTimeDouble(approvedMinutesLeftInMonth / 60.0),
                leftThisYear = TimeDoubleService.convertDoubleToTimeDouble(approvedMinutesLeftInYear / 60.0),
                leftComplete = TimeDoubleService.convertDoubleToTimeDouble(approvedMinutesLeftComplete / 60.0)
            )
        }

        return ApprovedHoursLeftResponseDTO(
            assistancePlanId = assistancePlan.id,
            hourMode = assistancePlan.hourMode,
            approvedHoursFrom = TimeDoubleService.convertDoubleToTimeDouble(approvedRangeMinutes.first / 60.0),
            approvedHoursTo = TimeDoubleService.convertDoubleToTimeDouble(approvedRangeMinutes.second / 60.0),
            hourTypeEvaluation = hourTypeEvaluation
        )
    }

    private fun getDistinctHourTypesIn(assistancePlan: AssistancePlan): Set<HourType> {
        if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            return assistancePlan.hourCorridor?.hourType?.let { setOf(it) } ?: emptySet()
        }
        val hourTypeIdsFromHours = assistancePlan.hours.mapNotNull { it.hourType }.toSet()
        val hourTypeIdsFromGoals = assistancePlan.goals.flatMap { goal -> goal.hours }.filter { it.hourType != null }
            .mapNotNull { it.hourType }.toSet()

        return hourTypeIdsFromHours + hourTypeIdsFromGoals
    }

    private fun getApprovedMinutesLeftIn(assistancePlan: AssistancePlan, hourTypeId: Long): Double {
        val executedMinutes = getExecutedMinutesByHourTypeIdIn(assistancePlan, hourTypeId)

        return if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            val approvedRangeMinutes = getApprovedCorridorMinutesRangeIn(
                assistancePlan.start,
                assistancePlan.end,
                assistancePlan,
                hourTypeId
            )
            calculateCorridorMinutesLeft(executedMinutes.toDouble(), approvedRangeMinutes.first, approvedRangeMinutes.second)
        } else {
            val approvedMinutes = getApprovedMinutesByHourTypeIdIn(assistancePlan, hourTypeId)
            approvedMinutes - executedMinutes
        }
    }

    private fun getApprovedMinutesLeftIn(date: LocalDate, assistancePlan: AssistancePlan, hourTypeId: Long): Double {
        val weekDates = getWeekStartAndEnd(date)
        val startOfTheWeek = weekDates.first
        val endOfTheWeek = weekDates.second
        val executedMinutes = getExecutedMinutesByHourTypeIdIn(startOfTheWeek, endOfTheWeek, assistancePlan, hourTypeId)

        return if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            val approvedRangeMinutes = getApprovedCorridorMinutesRangeIn(startOfTheWeek, endOfTheWeek, assistancePlan, hourTypeId)
            calculateCorridorMinutesLeft(executedMinutes.toDouble(), approvedRangeMinutes.first, approvedRangeMinutes.second)
        } else {
            val approvedMinutes = getApprovedMinutesByHourTypeIdIn(startOfTheWeek, endOfTheWeek, assistancePlan, hourTypeId)
            approvedMinutes - executedMinutes
        }
    }

    private fun getApprovedMinutesLeftIn(year: Int, assistancePlan: AssistancePlan, hourTypeId: Long): Double {
        val executedMinutes = getExecutedMinutesByHourTypeIdIn(year, assistancePlan, hourTypeId)

        return if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            val approvedRangeMinutes = getApprovedCorridorMinutesRangeIn(year, assistancePlan, hourTypeId)
            calculateCorridorMinutesLeft(executedMinutes.toDouble(), approvedRangeMinutes.first, approvedRangeMinutes.second)
        } else {
            val approvedMinutes = getApprovedMinutesByHourTypeIdIn(year, assistancePlan, hourTypeId)
            approvedMinutes - executedMinutes
        }
    }

    private fun getApprovedMinutesLeftIn(
        year: Int,
        month: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val executedMinutes = getExecutedMinutesByHourTypeIdIn(year, month, assistancePlan, hourTypeId)

        return if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            val approvedRangeMinutes = getApprovedCorridorMinutesRangeIn(year, month, assistancePlan, hourTypeId)
            calculateCorridorMinutesLeft(executedMinutes.toDouble(), approvedRangeMinutes.first, approvedRangeMinutes.second)
        } else {
            val approvedMinutes = getApprovedMinutesByHourTypeIdIn(year, month, assistancePlan, hourTypeId)
            approvedMinutes - executedMinutes
        }
    }

    private fun getApprovedMinutesByHourTypeIdIn(
        start: LocalDate,
        end: LocalDate,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            return getApprovedCorridorMinutesIn(start, end, assistancePlan, hourTypeId)
        }
        val days = countMatchingDaysIn(start, end, assistancePlan)

        val approvedMinutes = if (assistancePlan.hours.isEmpty()) {
            sumGoalsMinutesByHourTypeId(assistancePlan.goals, days, hourTypeId)
        } else {
            sumMinutesByHourTypeId(assistancePlan, days, hourTypeId)
        }

        return approvedMinutes
    }

    private fun getApprovedMinutesByHourTypeIdIn(
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            return getApprovedCorridorMinutesIn(assistancePlan, hourTypeId)
        }
        val days = DateService.countDaysOfYearBetweenStartAndEnd(
            assistancePlan.start,
            assistancePlan.end
        )

        val approvedMinutes = if (assistancePlan.hours.isEmpty()) {
            sumGoalsMinutesByHourTypeId(assistancePlan.goals, days, hourTypeId)
        } else {
            sumMinutesByHourTypeId(assistancePlan, days, hourTypeId)
        }
        return approvedMinutes
    }

    private fun getApprovedMinutesByHourTypeIdIn(
        year: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            return getApprovedCorridorMinutesIn(year, assistancePlan, hourTypeId)
        }
        val days = countMatchingDaysIn(year, assistancePlan)

        val approvedMinutes = if (assistancePlan.hours.isEmpty()) {
            sumGoalsMinutesByHourTypeId(assistancePlan.goals, days, hourTypeId)
        } else {
            sumMinutesByHourTypeId(assistancePlan, days, hourTypeId)
        }
        return approvedMinutes
    }

    private fun getApprovedMinutesByHourTypeIdIn(
        year: Int,
        month: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            return getApprovedCorridorMinutesIn(year, month, assistancePlan, hourTypeId)
        }
        val days = countMatchingDaysIn(year, month, assistancePlan)

        val approvedMinutes = if (assistancePlan.hours.isEmpty()) {
            sumGoalsMinutesByHourTypeId(assistancePlan.goals, days, hourTypeId)
        } else {
            sumMinutesByHourTypeId(assistancePlan, days, hourTypeId)
        }
        return approvedMinutes
    }

    private fun countMatchingDaysIn(year: Int, assistancePlan: AssistancePlan): Int {
        if (!isIn(year, assistancePlan)) {
            return 0
        }

        return DateService.countDaysOfYearBetweenStartAndEnd(
            year,
            assistancePlan.start,
            assistancePlan.end
        )
    }

    private fun countMatchingDaysIn(start: LocalDate, end: LocalDate, assistancePlan: AssistancePlan): Int {
        if (!isIn(start, end, assistancePlan)) {
            return 0
        }

        return DateService.countDaysOfYearBetweenStartAndEnd(
            start.year,
            start,
            end
        )
    }

    private fun countMatchingDaysIn(year: Int, month: Int, assistancePlan: AssistancePlan): Int {
        if (!isInYearMonth(year, month, assistancePlan)) {
            return 0
        }

        return DateService.countDaysOfMonthAndYearBetweenStartAndEnd(
            year,
            month,
            assistancePlan.start,
            assistancePlan.end
        )
    }

    private fun isIn(start: LocalDate, end: LocalDate, assistancePlan: AssistancePlan): Boolean {
        return assistancePlan.start <= end && assistancePlan.end >= start
    }

    private fun isIn(year: Int, assistancePlan: AssistancePlan): Boolean {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 1).plusMonths(1).minusDays(1)

        return assistancePlan.start <= end && assistancePlan.end >= start
    }

    private fun isInYearMonth(year: Int, month: Int, assistancePlan: AssistancePlan): Boolean {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return assistancePlan.start <= end && assistancePlan.end >= start
    }

    private fun sumMinutesByHourTypeId(assistancePlan: AssistancePlan, numberOfDays: Int, hourTypeId: Long): Double {
        return assistancePlan.hours.filter { it.hourType?.id == hourTypeId }
            .sumOf { hour -> (hour.weeklyMinutes / 7.0) * numberOfDays }
    }

    private fun sumGoalsMinutesByHourTypeId(goals: Set<Goal>, numberOfDays: Int, hourTypeId: Long): Double {
        return goals.sumOf { sumGoalMinutesByHourTypeId(it, numberOfDays, hourTypeId) }
    }

    private fun sumGoalMinutesByHourTypeId(goal: Goal, numberOfDays: Int, hourTypeId: Long): Double {
        if (goal.hours.isEmpty()) {
            return 0.0
        }

        val hours = goal.hours.filter { it.hourType?.id == hourTypeId }
        return hours.sumOf { hour -> (hour.weeklyMinutes / 7.0) * numberOfDays }
    }

    private fun getExecutedMinutesByHourTypeIdIn(
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Int {
        val services = serviceService.getAllByAssistancePlanIdAndHourTypeIdAndStartAndEnd(
            start = assistancePlan.start,
            end = assistancePlan.end,
            assistancePlanId = assistancePlan.id,
            hourTypeId = hourTypeId
        )
        return services.sumOf { it.minutes }
    }

    private fun getExecutedMinutesByHourTypeIdIn(
        start: LocalDate,
        end: LocalDate,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Int {
        val services = serviceService.getAllByAssistancePlanIdAndHourTypeIdAndStartAndEnd(
            start = start,
            end = end,
            assistancePlanId = assistancePlan.id,
            hourTypeId = hourTypeId
        )
        return services.sumOf { it.minutes }
    }

    private fun getExecutedMinutesByHourTypeIdIn(
        year: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Int {
        val services = serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(
            assistancePlanId = assistancePlan.id,
            hourTypeId = hourTypeId,
            year = year
        )
        return services.sumOf { it.minutes }
    }

    private fun getExecutedMinutesByHourTypeIdIn(
        year: Int,
        month: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Int {
        val services = serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(
            assistancePlanId = assistancePlan.id,
            hourTypeId = hourTypeId,
            year = year,
            month = month
        )
        return services.sumOf { it.minutes }
    }

    private fun getApprovedRangeMinutes(assistancePlan: AssistancePlan): Pair<Double, Double> {
        if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            val corridor = assistancePlan.hourCorridor
            val from = corridor?.weeklyMinutesFrom?.toDouble() ?: 0.0
            val till = corridor?.weeklyMinutesTill?.toDouble() ?: from
            return from to till
        }

        val approvedWeeklyMinutes = when {
            assistancePlan.hours.isNotEmpty() -> assistancePlan.hours.sumOf { it.weeklyMinutes.toDouble() }
            assistancePlan.goals.isNotEmpty() -> assistancePlan.goals.sumOf { goal ->
                goal.hours.sumOf { it.weeklyMinutes.toDouble() }
            }
            else -> 0.0
        }
        return approvedWeeklyMinutes to approvedWeeklyMinutes
    }

    private fun getApprovedCorridorMinutesIn(
        start: LocalDate,
        end: LocalDate,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val corridor = assistancePlan.hourCorridor ?: return 0.0
        if ((corridor.hourType?.id ?: 0) != hourTypeId) {
            return 0.0
        }
        val days = countMatchingDaysIn(start, end, assistancePlan)
        return corridorApprovedMinutes(corridor, days)
    }

    private fun getApprovedCorridorMinutesIn(
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val corridor = assistancePlan.hourCorridor ?: return 0.0
        if ((corridor.hourType?.id ?: 0) != hourTypeId) {
            return 0.0
        }
        val days = DateService.countDaysOfYearBetweenStartAndEnd(
            assistancePlan.start,
            assistancePlan.end
        )
        return corridorApprovedMinutes(corridor, days)
    }

    private fun getApprovedCorridorMinutesIn(
        year: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val corridor = assistancePlan.hourCorridor ?: return 0.0
        if ((corridor.hourType?.id ?: 0) != hourTypeId) {
            return 0.0
        }
        val days = countMatchingDaysIn(year, assistancePlan)
        return corridorApprovedMinutes(corridor, days)
    }

    private fun getApprovedCorridorMinutesIn(
        year: Int,
        month: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val corridor = assistancePlan.hourCorridor ?: return 0.0
        if ((corridor.hourType?.id ?: 0) != hourTypeId) {
            return 0.0
        }
        val days = countMatchingDaysIn(year, month, assistancePlan)
        return corridorApprovedMinutes(corridor, days)
    }

    private fun getApprovedCorridorMinutesRangeIn(
        start: LocalDate,
        end: LocalDate,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Pair<Double, Double> {
        val corridor = assistancePlan.hourCorridor ?: return 0.0 to 0.0
        if ((corridor.hourType?.id ?: 0) != hourTypeId) {
            return 0.0 to 0.0
        }
        val days = countMatchingDaysIn(start, end, assistancePlan)
        val from = corridorApprovedMinutesForDays(corridor, days, corridor.weeklyMinutesFrom)
        val till = corridorApprovedMinutesForDays(corridor, days, corridor.weeklyMinutesTill)
        return from to till
    }

    private fun getApprovedCorridorMinutesRangeIn(
        year: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Pair<Double, Double> {
        val corridor = assistancePlan.hourCorridor ?: return 0.0 to 0.0
        if ((corridor.hourType?.id ?: 0) != hourTypeId) {
            return 0.0 to 0.0
        }
        val days = countMatchingDaysIn(year, assistancePlan)
        val from = corridorApprovedMinutesForDays(corridor, days, corridor.weeklyMinutesFrom)
        val till = corridorApprovedMinutesForDays(corridor, days, corridor.weeklyMinutesTill)
        return from to till
    }

    private fun getApprovedCorridorMinutesRangeIn(
        year: Int,
        month: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Pair<Double, Double> {
        val corridor = assistancePlan.hourCorridor ?: return 0.0 to 0.0
        if ((corridor.hourType?.id ?: 0) != hourTypeId) {
            return 0.0 to 0.0
        }
        val days = countMatchingDaysIn(year, month, assistancePlan)
        val from = corridorApprovedMinutesForDays(corridor, days, corridor.weeklyMinutesFrom)
        val till = corridorApprovedMinutesForDays(corridor, days, corridor.weeklyMinutesTill)
        return from to till
    }

    private fun corridorApprovedMinutes(corridor: de.vinz.openfls.domains.hourCorridors.HourCorridor, days: Int): Double {
        val weeklyMinutesMean = (corridor.weeklyMinutesFrom + corridor.weeklyMinutesTill) / 2.0
        return weeklyMinutesMean / 7.0 * days
    }

    private fun corridorApprovedMinutesForDays(
        corridor: de.vinz.openfls.domains.hourCorridors.HourCorridor,
        days: Int,
        weeklyMinutes: Int
    ): Double {
        return weeklyMinutes / 7.0 * days
    }

    private fun calculateCorridorMinutesLeft(executedMinutes: Double, approvedMinutesFrom: Double, approvedMinutesTo: Double): Double {
        return when {
            executedMinutes < approvedMinutesFrom -> approvedMinutesFrom - executedMinutes
            executedMinutes > approvedMinutesTo -> approvedMinutesTo - executedMinutes
            else -> 0.0
        }
    }

    private fun getWeekStartAndEnd(date: LocalDate): Pair<LocalDate, LocalDate> {
        val startOfWeek = date.with(DayOfWeek.MONDAY)
        val endOfWeek = date.with(DayOfWeek.SUNDAY)

        return startOfWeek to endOfWeek
    }
}
