package de.vinz.openfls.domains.goalTimeEvaluations

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.goalTimeEvaluations.dtos.GoalTimeEvaluationDto
import de.vinz.openfls.domains.goalTimeEvaluations.dtos.GoalsTimeEvaluationDto
import de.vinz.openfls.domains.goalTimeEvaluations.exceptions.NoGoalFoundWithHourTypeException
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.goalTimeEvaluations.exceptions.AssistancePlanNotFoundException
import de.vinz.openfls.domains.goalTimeEvaluations.exceptions.YearOutOfRangeException
import de.vinz.openfls.domains.goalTimeEvaluations.models.YearMonthDoubleValue
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import kotlin.math.roundToInt

@Service
class GoalTimeEvaluationService(
        private val serviceRepository: ServiceRepository,
        private val assistancePlanRepository: AssistancePlanRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(GoalTimeEvaluationService::class.java)

    @Throws(AssistancePlanNotFoundException::class, NoGoalFoundWithHourTypeException::class)
    fun getByAssistancePlanIdAndHourTypeIdAndYear(assistancePlanId: Long,
                                                  hourTypeId: Long,
                                                  year: Int): GoalsTimeEvaluationDto {
        val assistancePlan = assistancePlanRepository
                .findById(assistancePlanId)
                .orElseThrow { AssistancePlanNotFoundException(assistancePlanId) }

        val goalsWithHourType = assistancePlan.goals
                .filter { it.hours.any { goalHour -> goalHour.hourType!!.id == hourTypeId } }

        if (goalsWithHourType.isEmpty() && assistancePlan.hours.none { it.hourType?.id == hourTypeId}) {
            throw NoGoalFoundWithHourTypeException(hourTypeId)
        }

        val start = assistancePlan.start
        val end = assistancePlan.end

        return try {
            val services = serviceRepository.findServicesByAssistancePlanIdAndStartIsBetween(
                    assistancePlanId,
                    LocalDateTime.of(start, LocalTime.of(0, 0, 0)),
                    LocalDateTime.of(end, LocalTime.of(23, 59, 59))
            )

            createGoalsTimeEvaluationDto(
                    assistancePlan,
                    goalsWithHourType,
                    year,
                    services,
                    hourTypeId,
                    start,
                    end
            )
        } catch (ex: YearOutOfRangeException) {
            createEmptyGoalsTimeEvaluationDto(assistancePlanId, goalsWithHourType)
        }
    }

    private fun createGoalsTimeEvaluationDto(
            assistancePlan: AssistancePlan,
            goalsWithHourType: List<Goal>,
            year: Int,
            services: List<de.vinz.openfls.domains.services.Service>,
            hourTypeId: Long,
            start: LocalDate,
            end: LocalDate
    ): GoalsTimeEvaluationDto {
        val executedHours = getMonthlyExecutedHoursInYear(assistancePlan, hourTypeId, start, end, year, services, false)
        val summedExecutedHours = getMonthlyExecutedHoursInYear(assistancePlan, hourTypeId, start, end, year, services, true)
        val approvedHours = getMonthlyApprovedHoursInYear(assistancePlan, hourTypeId, start, end, year, false)
        val summedApprovedHours = getMonthlyApprovedHoursInYear(assistancePlan, hourTypeId, start, end, year, true)

        val goalTimeEvaluations = if (goalsWithHourType.isNotEmpty()) {
            goalsWithHourType.map { goal ->
                createGoalTimeEvaluationDto(goal, hourTypeId, start, end, year, services)}.sortedBy { it.title }.toMutableList()
        } else {
            mutableListOf()
        }

        return GoalsTimeEvaluationDto().apply {
            this.assistancePlanId = assistancePlan.id
            this.executedHours = executedHours
            this.summedExecutedHours = summedExecutedHours
            this.approvedHours = approvedHours
            this.summedApprovedHours = summedApprovedHours
            this.approvedHoursLeft = getApprovedHoursLeft(approvedHours, executedHours).toMutableList()
            this.summedApprovedHoursLeft = getApprovedHoursLeft(summedApprovedHours, summedExecutedHours).toMutableList()
            this.goalTimeEvaluations = goalTimeEvaluations
        }
    }

    private fun createGoalTimeEvaluationDto(
            goal: Goal,
            hourTypeId: Long,
            start: LocalDate,
            end: LocalDate,
            year: Int,
            services: List<de.vinz.openfls.domains.services.Service>
    ): GoalTimeEvaluationDto {
        val executedHours = getMonthlyExecutedHoursInYear(goal, hourTypeId, start, end, year, services, false)
        val summedExecutedHours = getMonthlyExecutedHoursInYear(goal, hourTypeId, start, end, year, services, true)
        val approvedHours = getMonthlyApprovedHoursInYear(goal, hourTypeId, start, end, year, false)
        val summedApprovedHours = getMonthlyApprovedHoursInYear(goal, hourTypeId, start, end, year, true)

        return GoalTimeEvaluationDto().apply {
            this.id = goal.id
            this.title = goal.title
            this.description = goal.description
            this.executedHours = executedHours
            this.summedExecutedHours = summedExecutedHours
            this.approvedHours = approvedHours
            this.summedApprovedHours = summedApprovedHours
            this.approvedHoursLeft = getApprovedHoursLeft(approvedHours, executedHours).toMutableList()
            this.summedApprovedHoursLeft = getApprovedHoursLeft(summedApprovedHours, summedExecutedHours).toMutableList()
        }
    }

    private fun createEmptyGoalsTimeEvaluationDto(
            assistancePlanId: Long,
            goalsWithHourType: List<Goal>
    ): GoalsTimeEvaluationDto {
        val emptyHoursList = List(12) { 0.0 }

        val goalTimeEvaluations = if (goalsWithHourType.isNotEmpty()) {
            goalsWithHourType.map { goal ->
                createEmptyGoalTimeEvaluationDto(goal)
            }.sortedBy { it.title }.toMutableList()
        } else {
            mutableListOf()
        }

        logger.info(goalTimeEvaluations.size.toString())
        return GoalsTimeEvaluationDto().apply {
            this.assistancePlanId = assistancePlanId
            this.executedHours = emptyHoursList
            this.summedExecutedHours = emptyHoursList
            this.approvedHours = emptyHoursList
            this.summedApprovedHours = emptyHoursList
            this.approvedHoursLeft = emptyHoursList
            this.summedApprovedHoursLeft = emptyHoursList
            this.goalTimeEvaluations = goalTimeEvaluations
        }
    }

    private fun createEmptyGoalTimeEvaluationDto(goal: Goal): GoalTimeEvaluationDto {
        val emptyHoursList = List(12) { 0.0 }

        return GoalTimeEvaluationDto().apply {
            this.id = goal.id
            this.title = goal.title
            this.description = goal.description
            this.executedHours = emptyHoursList
            this.summedExecutedHours = emptyHoursList
            this.approvedHours = emptyHoursList
            this.summedApprovedHours = emptyHoursList
            this.approvedHoursLeft = emptyHoursList
            this.summedApprovedHoursLeft = emptyHoursList
        }
    }

    fun getMonthlyExecutedHoursInYear(assistancePlan: AssistancePlan,
                                      hourTypeId: Long,
                                      start: LocalDate,
                                      end: LocalDate,
                                      year: Int,
                                      services: List<de.vinz.openfls.domains.services.Service>,
                                      sum: Boolean): List<Double> {
        val executedMinutes = getExecutedMinutesMonthlyByYear(assistancePlan, hourTypeId, start, end, year, services, sum)
        return executedMinutes.map { DateService.convertMinutesToHour(it) }
    }

    fun getMonthlyExecutedHoursInYear(goal: Goal,
                                      hourTypeId: Long,
                                      start: LocalDate,
                                      end: LocalDate,
                                      year: Int,
                                      services: List<de.vinz.openfls.domains.services.Service>,
                                      sum: Boolean): List<Double> {
        val executedMinutes = getExecutedMinutesMonthlyByYear(goal, hourTypeId, start, end, year, services, sum)
        return executedMinutes.map { DateService.convertMinutesToHour(it) }
    }

    fun getExecutedMinutesMonthlyByYear(assistancePlan: AssistancePlan,
                                        hourTypeId: Long,
                                        start: LocalDate,
                                        end: LocalDate,
                                        year: Int,
                                        services: List<de.vinz.openfls.domains.services.Service>,
                                        sum: Boolean): List<Double> {
        val executedHours = getExecutedMinutesMonthly(assistancePlan, hourTypeId, start, end, services, sum)
        return getExecutedMinutesMonthlyByYear(year, executedHours)
    }

    fun getExecutedMinutesMonthlyByYear(goal: Goal,
                                        hourTypeId: Long,
                                        start: LocalDate,
                                        end: LocalDate,
                                        year: Int,
                                        services: List<de.vinz.openfls.domains.services.Service>,
                                        sum: Boolean): List<Double> {
        val executedHours = getExecutedMinutesMonthly(goal, hourTypeId, start, end, services, sum)
        return getExecutedMinutesMonthlyByYear(year, executedHours)
    }

    fun getExecutedMinutesMonthlyByYear(year: Int,
                                        executedHours: List<YearMonthDoubleValue>): List<Double> {
        val executedHoursInYear = executedHours.filter { it.yearMonth.year == year }.sortedBy { it.yearMonth }
        val result = getYearMonthValuesByYear(executedHoursInYear, year)

        return result.map { it.value }
    }

    fun getExecutedMinutesMonthly(assistancePlan: AssistancePlan,
                                  hourTypeId: Long,
                                  start: LocalDate,
                                  end: LocalDate,
                                  services: List<de.vinz.openfls.domains.services.Service>,
                                  sum: Boolean): List<YearMonthDoubleValue> {
        val filterService: (service: de.vinz.openfls.domains.services.Service) -> Boolean =
                { service -> service.assistancePlan?.id == assistancePlan.id }
        val minuteAdjustment: (service: de.vinz.openfls.domains.services.Service) -> Double =
                { service -> service.minutes.toDouble() }

        return getExecutedMinutesMonthly(
                start = start,
                end = end,
                services = services,
                hourTypeId = hourTypeId,
                sum = sum,
                filterService = filterService,
                minuteAdjustment = minuteAdjustment
        )
    }

    fun getExecutedMinutesMonthly(goal: Goal,
                                  hourTypeId: Long,
                                  start: LocalDate,
                                  end: LocalDate,
                                  services: List<de.vinz.openfls.domains.services.Service>,
                                  sum: Boolean): List<YearMonthDoubleValue> {
        val filterService: (service: de.vinz.openfls.domains.services.Service) -> Boolean =
                { service -> containsServiceGoal(service, goal) }
        val minuteAdjustment: (service: de.vinz.openfls.domains.services.Service) -> Double =
                { service -> (service.minutes.toDouble() / service.goals.size).roundToInt().toDouble() }

        return getExecutedMinutesMonthly(
                start = start,
                end = end,
                services = services,
                hourTypeId = hourTypeId,
                sum = sum,
                filterService = filterService,
                minuteAdjustment = minuteAdjustment
        )
    }

    fun getExecutedMinutesMonthly(
            start: LocalDate,
            end: LocalDate,
            services: List<de.vinz.openfls.domains.services.Service>,
            hourTypeId: Long,
            sum: Boolean,
            filterService: (service: de.vinz.openfls.domains.services.Service) -> Boolean,
            minuteAdjustment: (service: de.vinz.openfls.domains.services.Service) -> Double
    ): List<YearMonthDoubleValue> {
        val executedHours = YearMonthDoubleValue.getEmpty(start, end)
        val executedMinutesMap = executedHours.associate { it.yearMonth to it.value }.toMutableMap()

        val startTime = LocalDateTime.of(start, LocalTime.of(0, 0, 0))
        val endTime = LocalDateTime.of(end, LocalTime.of(23, 59, 59))

        for (service in services) {
            // invalid service
            if (!isServiceInBetween(service, startTime, endTime) || !isServiceHourType(service, hourTypeId) || !filterService(service)) {
                continue
            }

            val yearMonth = YearMonth.of(service.start.year, service.start.month)
            val existingValue = executedMinutesMap.getOrDefault(yearMonth, 0.0)
            executedMinutesMap[yearMonth] = existingValue + minuteAdjustment(service)
        }

        val yearMonthDoubleValues = executedMinutesMap.entries.map { YearMonthDoubleValue(it.key, it.value) }

        return if (sum) sumYearMonthDoubleValues(yearMonthDoubleValues) else yearMonthDoubleValues.sortedBy { it.yearMonth }
    }

    fun getMonthlyApprovedHoursInYear(assistancePlan: AssistancePlan,
                                      hourTypeId: Long,
                                      start: LocalDate,
                                      end: LocalDate,
                                      year: Int,
                                      sum: Boolean): List<Double> {
        val approvedMinutes = getApprovedHoursMonthly(assistancePlan, hourTypeId, start, end, sum)
        return getMonthlyApprovedHoursInYear(approvedMinutes, year)
    }

    private fun getMonthlyApprovedHoursInYear(goal: Goal,
                                              hourTypeId: Long,
                                              start: LocalDate,
                                              end: LocalDate,
                                              year: Int,
                                              sum: Boolean): List<Double> {
        val approvedMinutes = getApprovedHoursMonthly(goal, hourTypeId, start, end, sum)
        return getMonthlyApprovedHoursInYear(approvedMinutes, year)
    }

    private fun getMonthlyApprovedHoursInYear(approvedMinutes: List<YearMonthDoubleValue>,
                                              year: Int): List<Double> {
        val approvedMinutesInYear = approvedMinutes.filter { it.yearMonth.year == year }
        val result = getYearMonthValuesByYear(approvedMinutesInYear, year)

        return result.map { it.value }
    }

    private fun getApprovedHoursMonthly(assistancePlan: AssistancePlan,
                                        hourTypeId: Long,
                                        start: LocalDate,
                                        end: LocalDate,
                                        sum: Boolean): List<YearMonthDoubleValue> {
        val hourTypeExists = assistancePlan.hours.any { it.hourType!!.id == hourTypeId }

        val dailyHours = if (hourTypeExists) {
            ((assistancePlan.hours.first { it.hourType!!.id == hourTypeId }.weeklyHours) / 7)
        } else {
            0.0
        }

        return getApprovedHoursMonthly(dailyHours, start, end, sum)
    }

    private fun getApprovedHoursMonthly(goal: Goal,
                                        hourTypeId: Long,
                                        start: LocalDate,
                                        end: LocalDate,
                                        sum: Boolean): List<YearMonthDoubleValue> {
        val dailyHours = ((goal.hours.first { it.hourType!!.id == hourTypeId }.weeklyHours) / 7)
        return getApprovedHoursMonthly(dailyHours, start, end, sum)
    }

    private fun getApprovedHoursMonthly(dailyHours: Double,
                                        start: LocalDate,
                                        end: LocalDate,
                                        sum: Boolean): List<YearMonthDoubleValue> {
        var resultList = YearMonthDoubleValue.getEmpty(start, end)

        for (value in resultList) {
            val days = DateService.countDaysOfMonthAndYearBetweenStartAndEnd(
                    value.yearMonth.year,
                    value.yearMonth.monthValue,
                    start,
                    end)

            value.value = TimeDoubleService.convertDoubleToTimeDouble(days * dailyHours)
        }

        // sum up from previous months
        if (sum) {
            resultList = sumYearMonthDoubleValues(resultList)
        }

        return resultList.sortedBy { it.yearMonth }
    }

    fun getApprovedHoursLeft(approvedHours: List<Double>,
                             executedHours: List<Double>): List<Double> {
        val resultList = MutableList(approvedHours.size) { 0.0 }

        for (i in approvedHours.indices) {
            resultList[i] = TimeDoubleService.diffTimeDoubles(approvedHours[i], executedHours[i])
        }

        return resultList
    }

    private fun getYearMonthValuesByYear(values: List<YearMonthDoubleValue>, year: Int): List<YearMonthDoubleValue> {
        val result = getEmptyYearMonthDoubleByYear(year)

        for (resultExecutedHour in result) {
            try {
                val foundExecutedHour = values.first { it.yearMonth == resultExecutedHour.yearMonth }
                resultExecutedHour.value = foundExecutedHour.value
            } catch (_: NoSuchElementException) {
            }
        }

        return result
    }

    private fun sumYearMonthDoubleValues(valueList: List<YearMonthDoubleValue>): List<YearMonthDoubleValue> {
        val resultList = valueList.sortedBy { it.yearMonth }
        var actualValue = 0.0

        for (yearHourValue in valueList) {
            actualValue = TimeDoubleService.sumTimeDoubles(actualValue, yearHourValue.value)
            yearHourValue.value = actualValue
        }

        return resultList
    }

    private fun isServiceInBetween(service: de.vinz.openfls.domains.services.Service,
                                   start: LocalDateTime,
                                   end: LocalDateTime): Boolean {
        return service.start in start..end
    }

    private fun isServiceHourType(service: de.vinz.openfls.domains.services.Service, hourTypeId: Long): Boolean {
        return service.hourType?.id == hourTypeId
    }

    private fun containsServiceGoal(service: de.vinz.openfls.domains.services.Service, goal: Goal): Boolean {
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