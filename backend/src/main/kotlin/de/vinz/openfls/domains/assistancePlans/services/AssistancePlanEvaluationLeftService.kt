package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
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

        val hourTypeEvaluation = hourTypes.map { hourType ->
            val approvedHoursLeftInWeek = getApprovedHoursLeftIn(date, assistancePlan, hourType.id)
            val approvedHoursLeftInMonth =
                getApprovedHoursLeftIn(date.year, date.monthValue, assistancePlan, hourType.id)
            val approvedHoursLeftInYear = getApprovedHoursLeftIn(date.year, assistancePlan, hourType.id)

            HourTypeEvaluationDTO(
                hourTypeName = hourType.title,
                leftThisWeek = approvedHoursLeftInWeek,
                leftThisMonth = approvedHoursLeftInMonth,
                leftThisYear = approvedHoursLeftInYear
            )
        }

        return ApprovedHoursLeftResponseDTO(
            assistancePlanId = assistancePlan.id,
            hourTypeEvaluation = hourTypeEvaluation
        )
    }

    private fun getDistinctHourTypesIn(assistancePlan: AssistancePlan): Set<HourType> {
        val hourTypeIdsFromHours = assistancePlan.hours.mapNotNull { it.hourType }.toSet()
        val hourTypeIdsFromGoals = assistancePlan.goals.flatMap { goal -> goal.hours }.filter { it.hourType != null }
            .mapNotNull { it.hourType }.toSet()

        return hourTypeIdsFromHours + hourTypeIdsFromGoals
    }

    private fun getApprovedHoursLeftIn(date: LocalDate, assistancePlan: AssistancePlan, hourTypeId: Long): Double {
        val weekDates = getWeekStartAndEnd(date)
        val startOfTheWeek = weekDates.first
        val endOfTheWeek = weekDates.second
        val approvedHours = getApprovedHoursByHourTypeIdIn(startOfTheWeek, endOfTheWeek, assistancePlan, hourTypeId)
        val executedHours = getExecutedHoursByHourTypeIdIn(startOfTheWeek, endOfTheWeek, assistancePlan, hourTypeId)
        return TimeDoubleService.diffTimeDoubles(approvedHours, executedHours)
    }

    private fun getApprovedHoursLeftIn(year: Int, assistancePlan: AssistancePlan, hourTypeId: Long): Double {
        val approvedHours = getApprovedHoursByHourTypeIdIn(year, assistancePlan, hourTypeId)
        val executedHours = getExecutedHoursByHourTypeIdIn(year, assistancePlan, hourTypeId)
        return TimeDoubleService.diffTimeDoubles(approvedHours, executedHours)
    }

    private fun getApprovedHoursLeftIn(
        year: Int,
        month: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val approvedHours = getApprovedHoursByHourTypeIdIn(year, month, assistancePlan, hourTypeId)
        val executedHours = getExecutedHoursByHourTypeIdIn(year, month, assistancePlan, hourTypeId)
        return TimeDoubleService.diffTimeDoubles(approvedHours, executedHours)
    }

    private fun getApprovedHoursByHourTypeIdIn(
        start: LocalDate,
        end: LocalDate,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val days = countMatchingDaysIn(start, end, assistancePlan)

        val approvedHours = if (assistancePlan.hours.isEmpty()) {
            sumGoalsHoursByHourTypeId(assistancePlan.goals, days, hourTypeId)
        } else {
            sumHoursByHourTypeId(assistancePlan, days, hourTypeId)
        }

        return TimeDoubleService.convertDoubleToTimeDouble(approvedHours)
    }

    private fun getApprovedHoursByHourTypeIdIn(
        year: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val days = countMatchingDaysIn(year, assistancePlan)

        val approvedHours = if (assistancePlan.hours.isEmpty()) {
            sumGoalsHoursByHourTypeId(assistancePlan.goals, days, hourTypeId)
        } else {
            sumHoursByHourTypeId(assistancePlan, days, hourTypeId)
        }
        return TimeDoubleService.convertDoubleToTimeDouble(approvedHours)
    }

    private fun getApprovedHoursByHourTypeIdIn(
        year: Int,
        month: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val days = countMatchingDaysIn(year, month, assistancePlan)

        val approvedHours = if (assistancePlan.hours.isEmpty()) {
            sumGoalsHoursByHourTypeId(assistancePlan.goals, days, hourTypeId)
        } else {
            sumHoursByHourTypeId(assistancePlan, days, hourTypeId)
        }
        return TimeDoubleService.convertDoubleToTimeDouble(approvedHours)
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

    private fun sumHoursByHourTypeId(assistancePlan: AssistancePlan, days: Int, hourTypeId: Long): Double {
        return assistancePlan.hours.filter { it.hourType?.id == hourTypeId }
            .sumOf { hour -> (hour.weeklyHours / 7) * days }
    }

    private fun sumGoalsHoursByHourTypeId(goals: Set<Goal>, numberOfDays: Int, hourTypeId: Long): Double {
        return goals.sumOf { sumGoalHoursByHourTypeId(it, numberOfDays, hourTypeId) }
    }

    private fun sumGoalHoursByHourTypeId(goal: Goal, numberOfDays: Int, hourTypeId: Long): Double {
        if (goal.hours.isEmpty()) {
            return 0.0
        }

        val hours = goal.hours.filter { it.hourType?.id == hourTypeId }
        return hours.sumOf { hour -> (hour.weeklyHours / 7) * numberOfDays }
    }

    private fun getExecutedHoursByHourTypeIdIn(
        start: LocalDate,
        end: LocalDate,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val services = serviceService.getAllByAssistancePlanIdAndHourTypeIdAndStartAndEnd(
            start = start,
            end = end,
            assistancePlanId = assistancePlan.id,
            hourTypeId = hourTypeId
        )
        val hours = services.sumOf { it.minutes.toDouble() } / 60

        return TimeDoubleService.convertDoubleToTimeDouble(hours)
    }

    private fun getExecutedHoursByHourTypeIdIn(
        year: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val services = serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(
            assistancePlanId = assistancePlan.id,
            hourTypeId = hourTypeId,
            year = year
        )
        val hours = services.sumOf { it.minutes.toDouble() } / 60

        return TimeDoubleService.convertDoubleToTimeDouble(hours)
    }

    private fun getExecutedHoursByHourTypeIdIn(
        year: Int,
        month: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val services = serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(
            assistancePlanId = assistancePlan.id,
            hourTypeId = hourTypeId,
            year = year,
            month = month
        )
        val hours = services.sumOf { it.minutes.toDouble() } / 60

        return TimeDoubleService.convertDoubleToTimeDouble(hours)
    }

    private fun getWeekStartAndEnd(date: LocalDate): Pair<LocalDate, LocalDate> {
        val startOfWeek = date.with(DayOfWeek.MONDAY)
        val endOfWeek = date.with(DayOfWeek.SUNDAY)

        return startOfWeek to endOfWeek
    }
}