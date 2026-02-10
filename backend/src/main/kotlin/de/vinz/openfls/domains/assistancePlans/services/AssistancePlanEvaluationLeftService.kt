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
            val approvedMinutesLeftInWeek = getApprovedMinutesLeftIn(date, assistancePlan, hourType.id)
            val approvedMinutesLeftInMonth =
                getApprovedMinutesLeftIn(date.year, date.monthValue, assistancePlan, hourType.id)
            val approvedMinutesLeftInYear = getApprovedMinutesLeftIn(date.year, assistancePlan, hourType.id)

            HourTypeEvaluationDTO(
                hourTypeName = hourType.title,
                leftThisWeek = TimeDoubleService.convertDoubleToTimeDouble(approvedMinutesLeftInWeek / 60.0),
                leftThisMonth = TimeDoubleService.convertDoubleToTimeDouble(approvedMinutesLeftInMonth / 60.0),
                leftThisYear = TimeDoubleService.convertDoubleToTimeDouble(approvedMinutesLeftInYear / 60.0)
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

    private fun getApprovedMinutesLeftIn(date: LocalDate, assistancePlan: AssistancePlan, hourTypeId: Long): Double {
        val weekDates = getWeekStartAndEnd(date)
        val startOfTheWeek = weekDates.first
        val endOfTheWeek = weekDates.second
        val approvedMinutes = getApprovedMinutesByHourTypeIdIn(startOfTheWeek, endOfTheWeek, assistancePlan, hourTypeId)
        val executedMinutes = getExecutedMinutesByHourTypeIdIn(startOfTheWeek, endOfTheWeek, assistancePlan, hourTypeId)
        return approvedMinutes - executedMinutes
    }

    private fun getApprovedMinutesLeftIn(year: Int, assistancePlan: AssistancePlan, hourTypeId: Long): Double {
        val approvedMinutes = getApprovedMinutesByHourTypeIdIn(year, assistancePlan, hourTypeId)
        val executedMinutes = getExecutedMinutesByHourTypeIdIn(year, assistancePlan, hourTypeId)
        return approvedMinutes - executedMinutes
    }

    private fun getApprovedMinutesLeftIn(
        year: Int,
        month: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val approvedMinutes = getApprovedMinutesByHourTypeIdIn(year, month, assistancePlan, hourTypeId)
        val executedMinutes = getExecutedMinutesByHourTypeIdIn(year, month, assistancePlan, hourTypeId)
        return approvedMinutes - executedMinutes
    }

    private fun getApprovedMinutesByHourTypeIdIn(
        start: LocalDate,
        end: LocalDate,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val days = countMatchingDaysIn(start, end, assistancePlan)

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
        val days = countMatchingDaysIn(year, assistancePlan)

        val approvedHours = if (assistancePlan.hours.isEmpty()) {
            sumGoalsMinutesByHourTypeId(assistancePlan.goals, days, hourTypeId)
        } else {
            sumMinutesByHourTypeId(assistancePlan, days, hourTypeId)
        }
        return TimeDoubleService.convertDoubleToTimeDouble(approvedHours)
    }

    private fun getApprovedMinutesByHourTypeIdIn(
        year: Int,
        month: Int,
        assistancePlan: AssistancePlan,
        hourTypeId: Long
    ): Double {
        val days = countMatchingDaysIn(year, month, assistancePlan)

        val approvedHours = if (assistancePlan.hours.isEmpty()) {
            sumGoalsMinutesByHourTypeId(assistancePlan.goals, days, hourTypeId)
        } else {
            sumMinutesByHourTypeId(assistancePlan, days, hourTypeId)
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

    private fun sumMinutesByHourTypeId(assistancePlan: AssistancePlan, numberOfDays: Int, hourTypeId: Long): Double {
        return assistancePlan.hours.filter { it.hourType?.id == hourTypeId }
            .sumOf { hour -> (hour.weeklyHours / 7) * numberOfDays * 60.0 }
    }

    private fun sumGoalsMinutesByHourTypeId(goals: Set<Goal>, numberOfDays: Int, hourTypeId: Long): Double {
        return goals.sumOf { sumGoalMinutesByHourTypeId(it, numberOfDays, hourTypeId) }
    }

    private fun sumGoalMinutesByHourTypeId(goal: Goal, numberOfDays: Int, hourTypeId: Long): Double {
        if (goal.hours.isEmpty()) {
            return 0.0
        }

        val hours = goal.hours.filter { it.hourType?.id == hourTypeId }
        return hours.sumOf { hour -> (hour.weeklyHours / 7) * numberOfDays * 60.0 }
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

    private fun getWeekStartAndEnd(date: LocalDate): Pair<LocalDate, LocalDate> {
        val startOfWeek = date.with(DayOfWeek.MONDAY)
        val endOfWeek = date.with(DayOfWeek.SUNDAY)

        return startOfWeek to endOfWeek
    }
}