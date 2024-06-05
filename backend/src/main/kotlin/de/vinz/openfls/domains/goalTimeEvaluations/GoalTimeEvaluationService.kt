package de.vinz.openfls.domains.goalTimeEvaluations

import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.goalTimeEvaluations.dtos.GoalTimeEvaluationDto
import de.vinz.openfls.domains.goalTimeEvaluations.dtos.GoalsTimeEvaluationDto
import de.vinz.openfls.domains.goalTimeEvaluations.exceptions.NoGoalFoundWithHourTypeException
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.exceptions.AssistancePlanNotFoundException
import de.vinz.openfls.exceptions.YearOutOfRangeException
import de.vinz.openfls.models.YearMonthDoubleValue
import de.vinz.openfls.repositories.ServiceRepository
import de.vinz.openfls.services.ConverterService
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.NumberService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import kotlin.math.roundToInt

@Service
class GoalTimeEvaluationService(
        private val serviceRepository: ServiceRepository,
        private val assistancePlanRepository: AssistancePlanRepository,
        private val converterService: ConverterService
) {

    @Throws(AssistancePlanNotFoundException::class, NoGoalFoundWithHourTypeException::class)
    fun getByAssistancePlanIdAndHourTypeIdAndYear(assistancePlanId: Long,
                                                  hourTypeId: Long,
                                                  year: Int): GoalsTimeEvaluationDto {
        val assistancePlan =
                assistancePlanRepository
                        .findById(assistancePlanId)
                        .orElseThrow{ AssistancePlanNotFoundException(assistancePlanId)}
        val goalsWithHourType =
                assistancePlan.goals.filter { it.hours.any { goalHour -> goalHour.hourType!!.id == hourTypeId } }

        if (goalsWithHourType.isEmpty()) {
            throw NoGoalFoundWithHourTypeException(hourTypeId)
        }

        try {
            val services = serviceRepository.findServicesByAssistancePlanIdAndStartIsBetween(
                    assistancePlanId,
                    LocalDateTime.of(assistancePlan.start, LocalTime.of(0, 0, 0)),
                    LocalDateTime.of(assistancePlan.end, LocalTime.of(23, 59, 59)))

            return GoalsTimeEvaluationDto().also { goalTimeEvaluation ->
                goalTimeEvaluation.assistancePlanId = assistancePlanId
                goalTimeEvaluation.goalTimeEvaluations = goalsWithHourType
                        .map { goal ->
                            GoalTimeEvaluationDto().also {
                                it.id = goal.id
                                it.title = goal.title
                                it.description = goal.description
                                it.executedHours = getMonthlyExecutedHoursInYear(
                                        goal, hourTypeId, assistancePlan.start, assistancePlan.end, year, services, false)
                                it.summedExecutedHours = getMonthlyExecutedHoursInYear(
                                        goal, hourTypeId, assistancePlan.start, assistancePlan.end, year, services, true)
                                it.approvedHours = getMonthlyApprovedHoursInYear(
                                        goal, hourTypeId, assistancePlan.start, assistancePlan.end, year, false)
                                it.summedApprovedHours = getMonthlyApprovedHoursInYear(
                                        goal, hourTypeId, assistancePlan.start, assistancePlan.end, year, true)
                                it.approvedHoursLeft =
                                        getApprovedHoursLeft(it.approvedHours, it.executedHours).toMutableList()
                                it.summedApprovedHoursLeft =
                                        getApprovedHoursLeft(it.summedApprovedHours, it.summedExecutedHours).toMutableList()
                            }
                        }
                        .sortedBy { it.title }
                        .toMutableList()
            }
        } catch (ex: YearOutOfRangeException) {
            return GoalsTimeEvaluationDto().also { goalTimeEvaluation ->
                goalTimeEvaluation.assistancePlanId = assistancePlanId
                goalTimeEvaluation.goalTimeEvaluations = goalsWithHourType.map { goal ->
                    GoalTimeEvaluationDto().also {
                        it.id = goal.id
                        it.title = goal.title
                        it.description = goal.description
                        it.executedHours = List(12) { 0.0 }
                        it.summedExecutedHours = List(12) { 0.0 }
                        it.approvedHours = List(12) { 0.0 }
                        it.summedApprovedHours = List(12) { 0.0 }
                        it.approvedHoursLeft = List(12) { 0.0 }
                        it.summedApprovedHoursLeft = List(12) { 0.0 }
                    }
                }.sortedBy { it.title }.toMutableList()
            }
        }
    }

    fun getMonthlyExecutedHoursInYear(goal: Goal,
                                      hourTypeId: Long,
                                      start: LocalDate,
                                      end: LocalDate,
                                      year: Int,
                                      services: List<de.vinz.openfls.entities.Service>,
                                      sum: Boolean): List<Double> {
        val executedMinutes = getExecutedMinutesMonthlyByYear(goal, hourTypeId, start, end, year, services, sum)
        return executedMinutes.map { converterService.convertMinutesToHour(it) }
    }

    fun getExecutedMinutesMonthlyByYear(goal: Goal,
                                        hourTypeId: Long,
                                        start: LocalDate,
                                        end: LocalDate,
                                        year: Int,
                                        services: List<de.vinz.openfls.entities.Service>,
                                        sum: Boolean): List<Double> {
        val executedHours = getExecutedMinutesMonthly(goal, hourTypeId, start, end, services, sum)
        val executedHoursInYear = executedHours.filter { it.yearMonth.year == year }.sortedBy { it.yearMonth }
        val result = getYearMonthValuesByYear(executedHoursInYear, year)

        return result.map { it.value }
    }

    fun getExecutedMinutesMonthly(goal: Goal,
                                  hourTypeId: Long,
                                  start: LocalDate,
                                  end: LocalDate,
                                  services: List<de.vinz.openfls.entities.Service>,
                                  sum: Boolean): List<YearMonthDoubleValue> {
        val executedHours = YearMonthDoubleValue.getEmpty(start, end)
        val executedMinutesMap = HashMap<YearMonth, Double>()
        executedHours.forEach { executedMinutesMap[it.yearMonth] = it.value }

        val startTime = LocalDateTime.of(start, LocalTime.of(0, 0, 0))
        val endTime = LocalDateTime.of(end, LocalTime.of(23, 59, 59))

        for (service in services) {
            // invalid service
            if (!isServiceInBetween(service, startTime, endTime) ||
                    !isServiceHourType(service, hourTypeId) ||
                    !containsServiceGoal(service, goal)) {
                continue
            }

            val yearMonth = YearMonth.of(service.start.year, service.start.month)
            val existingValue = if (executedMinutesMap.containsKey(yearMonth)) executedMinutesMap.getValue(yearMonth) else 0.0
            executedMinutesMap[yearMonth] = existingValue + (service.minutes.toDouble() / service.goals.size).roundToInt()
        }

        val yearMonthDoubleValues = executedMinutesMap.entries.map { YearMonthDoubleValue(it.key, it.value) }

        if (sum) {
            return sumYearMonthDoubleValues(yearMonthDoubleValues)
        }

        return yearMonthDoubleValues.sortedBy { it.yearMonth }
    }

    private fun getMonthlyApprovedHoursInYear(goal: Goal,
                                              hourTypeId: Long,
                                              start: LocalDate,
                                              end: LocalDate,
                                              year: Int,
                                              sum: Boolean): List<Double> {
        val approvedMinutes = getApprovedHoursMonthly(goal, hourTypeId, start, end, sum)
        val approvedMinutesInYear = approvedMinutes.filter { it.yearMonth.year == year }
        val result = getYearMonthValuesByYear(approvedMinutesInYear ,year)

        return result.map { it.value }
    }

    private fun getApprovedHoursMonthly(goal: Goal,
                                        hourTypeId: Long,
                                        start: LocalDate,
                                        end: LocalDate,
                                        sum: Boolean): List<YearMonthDoubleValue> {
        val dailyHours = ((goal.hours.first { it.hourType!!.id == hourTypeId }.weeklyHours) / 7)
        var resultList = YearMonthDoubleValue.getEmpty(start, end)

        for (value in resultList) {
            val days = DateService.countDaysOfMonthAndYearBetweenStartAndEnd(
                    value.yearMonth.year,
                    value.yearMonth.monthValue,
                    start,
                    end)

            value.value = NumberService.convertDoubleToTimeDouble(days * dailyHours)
        }

        // sum up from previous months
        if (sum) {
            resultList = sumYearMonthDoubleValues(resultList)
        }

        return resultList.sortedBy { it.yearMonth }
    }

    private fun getApprovedHoursLeft(approvedHours: List<Double>,
                                     executedHours: List<Double>): List<Double> {
        val resultList = MutableList(approvedHours.size) { 0.0 }

        for (i in approvedHours.indices) {
            val approvedMinutes = converterService.convertHourToMinutes(approvedHours[i])
            val executedMinutes = converterService.convertHourToMinutes(executedHours[i])
            if (approvedMinutes > 0) {
                val differenceMinutes = approvedMinutes - executedMinutes
                resultList[i] = converterService.convertMinutesToHour(differenceMinutes.toDouble())
            } else {
                resultList[i] = 0.0
            }
        }

        return resultList
    }

    private fun getYearMonthValuesByYear(values: List<YearMonthDoubleValue>, year: Int): List<YearMonthDoubleValue> {
        val result = getEmptyYearMonthDoubleByYear(year)

        for (resultExecutedHour in result) {
            try {
                val foundExecutedHour = values.first { it.yearMonth == resultExecutedHour.yearMonth }
                resultExecutedHour.value = foundExecutedHour.value
            } catch (ex: NoSuchElementException) {
            }
        }

        return result
    }

    private fun sumYearMonthDoubleValues(valueList: List<YearMonthDoubleValue>): List<YearMonthDoubleValue> {
        val resultList = valueList.sortedBy { it.yearMonth }
        var actualValue = 0.0

        for (yearHourValue in valueList) {
            actualValue = NumberService.sumTimeDoubles(actualValue, yearHourValue.value)
            yearHourValue.value = actualValue
        }

        return resultList
    }

    private fun isServiceInBetween(service: de.vinz.openfls.entities.Service,
                                   start: LocalDateTime,
                                   end: LocalDateTime): Boolean {
        return service.start in start..end
    }

    private fun isServiceHourType(service: de.vinz.openfls.entities.Service, hourTypeId: Long): Boolean {
        return service.hourType?.id == hourTypeId
    }

    private fun containsServiceGoal(service: de.vinz.openfls.entities.Service, goal: Goal): Boolean {
        return service.goals.any { it.id == goal.id }
    }

    private fun getEmptyYearMonthDoubleByYear(year: Int): List<YearMonthDoubleValue> {
        val resultList = mutableListOf<YearMonthDoubleValue>()
        for (i in 1..12) {
            resultList.add(YearMonthDoubleValue(YearMonth.of(year, i), 0.0))
        }

        return resultList
    }
}