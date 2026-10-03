package de.vinz.openfls.domains.goalTimeEvaluations.service

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.goalTimeEvaluations.YearMonthDoubleValue
import de.vinz.openfls.domains.goalTimeEvaluations.dto.GoalTimeEvaluationResponse
import de.vinz.openfls.domains.goalTimeEvaluations.dto.GoalTimeEvaluationResult
import de.vinz.openfls.domains.goalTimeEvaluations.dto.GoalsTimeEvaluationResponse
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.services.service.ServiceService
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import kotlin.math.roundToInt

@Service
@Transactional(readOnly = true)
class GoalTimeEvaluationService(
        private val serviceService: ServiceService,
        private val assistancePlanService: AssistancePlanService
) {

    /**
     * Die drei Hilfeplan-Zeilen bei Korridor-Plänen inkl. der Ableitung der wöchentlichen
     * Minuten aus den Korridorgrenzen (von, bis).
     */
    private val corridorRowDefinitions: List<Pair<String, (Int, Int) -> Double>> = listOf(
            "Untergrenze" to { from, _ -> from.toDouble() },
            "Obergrenze" to { _, till -> till.toDouble() },
            "Durchschnitt" to { from, till -> (from + till) / 2.0 }
    )

    fun getByAssistancePlanIdAndHourTypeIdAndYear(assistancePlanId: Long,
                                                  hourTypeId: Long,
                                                  year: Int): GoalTimeEvaluationResult {
        val assistancePlan = assistancePlanService.getEntityById(assistancePlanId)
                ?: return GoalTimeEvaluationResult.AssistancePlanNotFound

        if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            return GoalTimeEvaluationResult.Success(
                    buildCorridorEvaluationResponse(assistancePlan, hourTypeId, year))
        }

        val goalsWithHourType = assistancePlan.goals
                .filter { it.hours.any { goalHour -> goalHour.hourType!!.id == hourTypeId } }

        if (goalsWithHourType.isEmpty() && assistancePlan.hours.none { it.hourType?.id == hourTypeId}) {
            return GoalTimeEvaluationResult.NoGoalFoundForHourType
        }

        val start = assistancePlan.start
        val end = assistancePlan.end

        val services = serviceService.getAllEntitiesByAssistancePlanIdAndStartBetween(
                assistancePlanId,
                LocalDateTime.of(start, LocalTime.of(0, 0, 0)),
                LocalDateTime.of(end, LocalTime.of(23, 59, 59))
        )

        return GoalTimeEvaluationResult.Success(buildExactModeEvaluationResponse(
                assistancePlan,
                goalsWithHourType,
                year,
                services,
                hourTypeId,
                start,
                end
        ))
    }

    /**
     * Zeitauswertung für Korridor-Hilfepläne.
     *
     * Korridor-Pläne besitzen weder Plan- noch Zielstunden; die genehmigte Wochenminuten-
     * Spanne liegt ausschließlich auf dem [de.vinz.openfls.domains.hourCorridors.entity.HourCorridor].
     * Daraus werden für den Hilfeplan drei Zeilen erzeugt (Untergrenze, Obergrenze,
     * Durchschnitt). Die Ziele erhalten nur executedHours/summedExecutedHours; alle
     * genehmigten Werte sind 0. Wird ein anderer Stundentyp als der des Korridors
     * angefragt, ist das Ergebnis leer (alle Werte 0), aber kein Fehler.
     */
    private fun buildCorridorEvaluationResponse(
            assistancePlan: AssistancePlan,
            hourTypeId: Long,
            year: Int
    ): GoalsTimeEvaluationResponse {
        val start = assistancePlan.start
        val end = assistancePlan.end
        val corridor = assistancePlan.hourCorridor
        val activeCorridor = corridor?.takeIf { it.hourType?.id == hourTypeId }

        val services = if (activeCorridor != null) {
            serviceService.getAllEntitiesByAssistancePlanIdAndStartBetween(
                    assistancePlan.id,
                    LocalDateTime.of(start, LocalTime.of(0, 0, 0)),
                    LocalDateTime.of(end, LocalTime.of(23, 59, 59))
            )
        } else {
            emptyList()
        }

        val executedHours = if (activeCorridor != null)
            getExecutedHoursByMonthInYearForAssistancePlan(assistancePlan, hourTypeId, start, end, year, services, false)
        else zeroHoursPerMonth()
        val summedExecutedHours = if (activeCorridor != null)
            getExecutedHoursByMonthInYearForAssistancePlan(assistancePlan, hourTypeId, start, end, year, services, true)
        else zeroHoursPerMonth()

        val goalEvaluations = assistancePlan.goals
                .map { goal -> buildCorridorGoalRow(goal, hourTypeId, start, end, year, services, activeCorridor != null) }
                .sortedBy { it.title }

        val corridorRows = corridorRowDefinitions.map { (title, weeklyMinutesOf) ->
            if (activeCorridor != null) {
                buildCorridorPlanRow(
                        title = title,
                        weeklyMinutes = weeklyMinutesOf(activeCorridor.weeklyMinutesFrom, activeCorridor.weeklyMinutesTill),
                        start = start,
                        end = end,
                        year = year,
                        executedHours = executedHours,
                        summedExecutedHours = summedExecutedHours
                )
            } else {
                emptyGoalRow(title = title)
            }
        }

        return GoalsTimeEvaluationResponse(
                assistancePlanId = assistancePlan.id,
                hourMode = AssistancePlanHourMode.CORRIDOR,
                executedHours = executedHours,
                summedExecutedHours = summedExecutedHours,
                approvedHours = zeroHoursPerMonth(),
                summedApprovedHours = zeroHoursPerMonth(),
                approvedHoursLeft = zeroHoursPerMonth(),
                summedApprovedHoursLeft = zeroHoursPerMonth(),
                goalTimeEvaluations = goalEvaluations,
                corridorAssistancePlanEvaluations = corridorRows
        )
    }

    private fun buildCorridorGoalRow(
            goal: Goal,
            hourTypeId: Long,
            start: LocalDate,
            end: LocalDate,
            year: Int,
            services: List<de.vinz.openfls.domains.services.entity.Service>,
            matchesHourType: Boolean
    ): GoalTimeEvaluationResponse {
        val executedHours = if (matchesHourType)
            getExecutedHoursByMonthInYearForGoal(goal, hourTypeId, start, end, year, services, false)
        else zeroHoursPerMonth()
        val summedExecutedHours = if (matchesHourType)
            getExecutedHoursByMonthInYearForGoal(goal, hourTypeId, start, end, year, services, true)
        else zeroHoursPerMonth()

        return GoalTimeEvaluationResponse(
                id = goal.id,
                title = goal.title,
                description = goal.description,
                executedHours = executedHours,
                summedExecutedHours = summedExecutedHours,
                approvedHours = zeroHoursPerMonth(),
                summedApprovedHours = zeroHoursPerMonth(),
                approvedHoursLeft = zeroHoursPerMonth(),
                summedApprovedHoursLeft = zeroHoursPerMonth()
        )
    }

    private fun buildCorridorPlanRow(
            title: String,
            weeklyMinutes: Double,
            start: LocalDate,
            end: LocalDate,
            year: Int,
            executedHours: List<Double>,
            summedExecutedHours: List<Double>
    ): GoalTimeEvaluationResponse {
        val dailyHours = (weeklyMinutes / 7.0) / 60.0
        val approvedHours = restrictToCalendarYear(calculateApprovedHoursByMonth(dailyHours, start, end, false), year)
        val summedApprovedHours = restrictToCalendarYear(calculateApprovedHoursByMonth(dailyHours, start, end, true), year)

        return GoalTimeEvaluationResponse(
                id = 0,
                title = title,
                description = "",
                executedHours = executedHours,
                summedExecutedHours = summedExecutedHours,
                approvedHours = approvedHours,
                summedApprovedHours = summedApprovedHours,
                approvedHoursLeft = calculateApprovedHoursLeft(approvedHours, executedHours),
                summedApprovedHoursLeft = calculateApprovedHoursLeft(summedApprovedHours, summedExecutedHours)
        )
    }

    private fun emptyGoalRow(title: String): GoalTimeEvaluationResponse {
        return GoalTimeEvaluationResponse(
                id = 0,
                title = title,
                description = "",
                executedHours = zeroHoursPerMonth(),
                summedExecutedHours = zeroHoursPerMonth(),
                approvedHours = zeroHoursPerMonth(),
                summedApprovedHours = zeroHoursPerMonth(),
                approvedHoursLeft = zeroHoursPerMonth(),
                summedApprovedHoursLeft = zeroHoursPerMonth()
        )
    }

    private fun zeroHoursPerMonth(): List<Double> = List(12) { 0.0 }

    private fun buildExactModeEvaluationResponse(
            assistancePlan: AssistancePlan,
            goalsWithHourType: List<Goal>,
            year: Int,
            services: List<de.vinz.openfls.domains.services.entity.Service>,
            hourTypeId: Long,
            start: LocalDate,
            end: LocalDate
    ): GoalsTimeEvaluationResponse {
        val executedHours = getExecutedHoursByMonthInYearForAssistancePlan(assistancePlan, hourTypeId, start, end, year, services, false)
        val summedExecutedHours = getExecutedHoursByMonthInYearForAssistancePlan(assistancePlan, hourTypeId, start, end, year, services, true)
        val approvedHours = getApprovedHoursByMonthInYearForAssistancePlan(assistancePlan, hourTypeId, start, end, year, false)
        val summedApprovedHours = getApprovedHoursByMonthInYearForAssistancePlan(assistancePlan, hourTypeId, start, end, year, true)

        val goalTimeEvaluations = goalsWithHourType
                .map { goal -> buildGoalRow(goal, hourTypeId, start, end, year, services) }
                .sortedBy { it.title }

        return GoalsTimeEvaluationResponse(
                assistancePlanId = assistancePlan.id,
                hourMode = AssistancePlanHourMode.EXACT,
                executedHours = executedHours,
                summedExecutedHours = summedExecutedHours,
                approvedHours = approvedHours,
                summedApprovedHours = summedApprovedHours,
                approvedHoursLeft = calculateApprovedHoursLeft(approvedHours, executedHours),
                summedApprovedHoursLeft = calculateApprovedHoursLeft(summedApprovedHours, summedExecutedHours),
                goalTimeEvaluations = goalTimeEvaluations
        )
    }

    private fun buildGoalRow(
            goal: Goal,
            hourTypeId: Long,
            start: LocalDate,
            end: LocalDate,
            year: Int,
            services: List<de.vinz.openfls.domains.services.entity.Service>
    ): GoalTimeEvaluationResponse {
        val executedHours = getExecutedHoursByMonthInYearForGoal(goal, hourTypeId, start, end, year, services, false)
        val summedExecutedHours = getExecutedHoursByMonthInYearForGoal(goal, hourTypeId, start, end, year, services, true)
        val approvedHours = getApprovedHoursByMonthInYearForGoal(goal, hourTypeId, start, end, year, false)
        val summedApprovedHours = getApprovedHoursByMonthInYearForGoal(goal, hourTypeId, start, end, year, true)

        return GoalTimeEvaluationResponse(
                id = goal.id,
                title = goal.title,
                description = goal.description,
                executedHours = executedHours,
                summedExecutedHours = summedExecutedHours,
                approvedHours = approvedHours,
                summedApprovedHours = summedApprovedHours,
                approvedHoursLeft = calculateApprovedHoursLeft(approvedHours, executedHours),
                summedApprovedHoursLeft = calculateApprovedHoursLeft(summedApprovedHours, summedExecutedHours)
        )
    }

    private fun getExecutedHoursByMonthInYearForAssistancePlan(assistancePlan: AssistancePlan,
                                      hourTypeId: Long,
                                      start: LocalDate,
                                      end: LocalDate,
                                      year: Int,
                                      services: List<de.vinz.openfls.domains.services.entity.Service>,
                                      sum: Boolean): List<Double> {
        val executedMinutes = getExecutedMinutesByMonthInYearForAssistancePlan(assistancePlan, hourTypeId, start, end, year, services, sum)
        return executedMinutes.map { DateService.convertMinutesToHour(it) }
    }

    private fun getExecutedHoursByMonthInYearForGoal(goal: Goal,
                                      hourTypeId: Long,
                                      start: LocalDate,
                                      end: LocalDate,
                                      year: Int,
                                      services: List<de.vinz.openfls.domains.services.entity.Service>,
                                      sum: Boolean): List<Double> {
        val executedMinutes = getExecutedMinutesByMonthInYearForGoal(goal, hourTypeId, start, end, year, services, sum)
        return executedMinutes.map { DateService.convertMinutesToHour(it) }
    }

    private fun getExecutedMinutesByMonthInYearForAssistancePlan(assistancePlan: AssistancePlan,
                                        hourTypeId: Long,
                                        start: LocalDate,
                                        end: LocalDate,
                                        year: Int,
                                        services: List<de.vinz.openfls.domains.services.entity.Service>,
                                        sum: Boolean): List<Double> {
        val executedMinutes = getExecutedMinutesByMonthForAssistancePlan(assistancePlan, hourTypeId, start, end, services, sum)
        return restrictToCalendarYear(executedMinutes, year)
    }

    private fun getExecutedMinutesByMonthInYearForGoal(goal: Goal,
                                        hourTypeId: Long,
                                        start: LocalDate,
                                        end: LocalDate,
                                        year: Int,
                                        services: List<de.vinz.openfls.domains.services.entity.Service>,
                                        sum: Boolean): List<Double> {
        val executedMinutes = getExecutedMinutesByMonthForGoal(goal, hourTypeId, start, end, services, sum)
        return restrictToCalendarYear(executedMinutes, year)
    }

    /**
     * Schneidet eine über den gesamten Planzeitraum berechnete Monatsreihe auf genau ein
     * Kalenderjahr zu (12 Werte, fehlende Monate = 0).
     */
    private fun restrictToCalendarYear(values: List<YearMonthDoubleValue>, year: Int): List<Double> {
        val valuesInYear = values.filter { it.yearMonth.year == year }
        val result = fillYearMonths(valuesInYear, year)

        return result.map { it.value }
    }

    private fun getExecutedMinutesByMonthForAssistancePlan(assistancePlan: AssistancePlan,
                                  hourTypeId: Long,
                                  start: LocalDate,
                                  end: LocalDate,
                                  services: List<de.vinz.openfls.domains.services.entity.Service>,
                                  sum: Boolean): List<YearMonthDoubleValue> {
        val filterService: (service: de.vinz.openfls.domains.services.entity.Service) -> Boolean =
                { service -> service.assistancePlan?.id == assistancePlan.id }
        val minuteAdjustment: (service: de.vinz.openfls.domains.services.entity.Service) -> Double =
                { service -> service.minutes.toDouble() }

        return aggregateServiceMinutesByMonth(
                start = start,
                end = end,
                services = services,
                hourTypeId = hourTypeId,
                sum = sum,
                filterService = filterService,
                minuteAdjustment = minuteAdjustment
        )
    }

    private fun getExecutedMinutesByMonthForGoal(goal: Goal,
                                  hourTypeId: Long,
                                  start: LocalDate,
                                  end: LocalDate,
                                  services: List<de.vinz.openfls.domains.services.entity.Service>,
                                  sum: Boolean): List<YearMonthDoubleValue> {
        val filterService: (service: de.vinz.openfls.domains.services.entity.Service) -> Boolean =
                { service -> serviceIncludesGoal(service, goal) }
        val minuteAdjustment: (service: de.vinz.openfls.domains.services.entity.Service) -> Double =
                { service -> (service.minutes.toDouble() / service.goals.size).roundToInt().toDouble() }

        return aggregateServiceMinutesByMonth(
                start = start,
                end = end,
                services = services,
                hourTypeId = hourTypeId,
                sum = sum,
                filterService = filterService,
                minuteAdjustment = minuteAdjustment
        )
    }

    private fun aggregateServiceMinutesByMonth(
            start: LocalDate,
            end: LocalDate,
            services: List<de.vinz.openfls.domains.services.entity.Service>,
            hourTypeId: Long,
            sum: Boolean,
            filterService: (service: de.vinz.openfls.domains.services.entity.Service) -> Boolean,
            minuteAdjustment: (service: de.vinz.openfls.domains.services.entity.Service) -> Double
    ): List<YearMonthDoubleValue> {
        val executedHours = YearMonthDoubleValue.getEmpty(start, end)
        val executedMinutesMap = executedHours.associate { it.yearMonth to it.value }.toMutableMap()

        val startTime = LocalDateTime.of(start, LocalTime.of(0, 0, 0))
        val endTime = LocalDateTime.of(end, LocalTime.of(23, 59, 59))

        for (service in services) {
            // invalid service
            if (!isServiceWithinPeriod(service, startTime, endTime) || !matchesHourType(service, hourTypeId) || !filterService(service)) {
                continue
            }

            val yearMonth = YearMonth.of(service.start.year, service.start.month)
            val existingValue = executedMinutesMap.getOrDefault(yearMonth, 0.0)
            executedMinutesMap[yearMonth] = existingValue + minuteAdjustment(service)
        }

        val yearMonthDoubleValues = executedMinutesMap.entries.map { YearMonthDoubleValue(it.key, it.value) }

        return if (sum) accumulateYearMonthValues(yearMonthDoubleValues) else yearMonthDoubleValues.sortedBy { it.yearMonth }
    }

    private fun getApprovedHoursByMonthInYearForAssistancePlan(assistancePlan: AssistancePlan,
                                      hourTypeId: Long,
                                      start: LocalDate,
                                      end: LocalDate,
                                      year: Int,
                                      sum: Boolean): List<Double> {
        val approvedMinutes = getApprovedHoursByMonthForAssistancePlan(assistancePlan, hourTypeId, start, end, sum)
        return restrictToCalendarYear(approvedMinutes, year)
    }

    private fun getApprovedHoursByMonthInYearForGoal(goal: Goal,
                                              hourTypeId: Long,
                                              start: LocalDate,
                                              end: LocalDate,
                                              year: Int,
                                              sum: Boolean): List<Double> {
        val approvedMinutes = getApprovedHoursByMonthForGoal(goal, hourTypeId, start, end, sum)
        return restrictToCalendarYear(approvedMinutes, year)
    }

    private fun getApprovedHoursByMonthForAssistancePlan(assistancePlan: AssistancePlan,
                                        hourTypeId: Long,
                                        start: LocalDate,
                                        end: LocalDate,
                                        sum: Boolean): List<YearMonthDoubleValue> {
        val hourTypeExists = assistancePlan.hours.any { it.hourType!!.id == hourTypeId }

        val dailyHours = if (hourTypeExists) {
            ((assistancePlan.hours.first { it.hourType!!.id == hourTypeId }.weeklyMinutes) / 7.0) / 60.0
        } else {
            0.0
        }

        return calculateApprovedHoursByMonth(dailyHours, start, end, sum)
    }

    private fun getApprovedHoursByMonthForGoal(goal: Goal,
                                        hourTypeId: Long,
                                        start: LocalDate,
                                        end: LocalDate,
                                        sum: Boolean): List<YearMonthDoubleValue> {
        val dailyHours = ((goal.hours.first { it.hourType!!.id == hourTypeId }.weeklyMinutes) / 7.0) / 60.0
        return calculateApprovedHoursByMonth(dailyHours, start, end, sum)
    }

    private fun calculateApprovedHoursByMonth(dailyHours: Double,
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
            resultList = accumulateYearMonthValues(resultList)
        }

        return resultList.sortedBy { it.yearMonth }
    }

    private fun calculateApprovedHoursLeft(approvedHours: List<Double>,
                             executedHours: List<Double>): List<Double> {
        val resultList = MutableList(approvedHours.size) { 0.0 }

        for (i in approvedHours.indices) {
            resultList[i] = TimeDoubleService.diffTimeDoubles(approvedHours[i], executedHours[i])
        }

        return resultList
    }

    private fun fillYearMonths(values: List<YearMonthDoubleValue>, year: Int): List<YearMonthDoubleValue> {
        val result = zeroedMonthsForCalendarYear(year)

        for (resultExecutedHour in result) {
            try {
                val foundExecutedHour = values.first { it.yearMonth == resultExecutedHour.yearMonth }
                resultExecutedHour.value = foundExecutedHour.value
            } catch (_: NoSuchElementException) {
            }
        }

        return result
    }

    private fun accumulateYearMonthValues(valueList: List<YearMonthDoubleValue>): List<YearMonthDoubleValue> {
        val resultList = valueList.sortedBy { it.yearMonth }
        var actualValue = 0.0

        for (yearHourValue in valueList) {
            actualValue = TimeDoubleService.sumTimeDoubles(actualValue, yearHourValue.value)
            yearHourValue.value = actualValue
        }

        return resultList
    }

    private fun isServiceWithinPeriod(service: de.vinz.openfls.domains.services.entity.Service,
                                   start: LocalDateTime,
                                   end: LocalDateTime): Boolean {
        return service.start in start..end
    }

    private fun matchesHourType(service: de.vinz.openfls.domains.services.entity.Service, hourTypeId: Long): Boolean {
        return service.hourType?.id == hourTypeId
    }

    private fun serviceIncludesGoal(service: de.vinz.openfls.domains.services.entity.Service, goal: Goal): Boolean {
        return service.goals.any { it.id == goal.id }
    }

    private fun zeroedMonthsForCalendarYear(year: Int): List<YearMonthDoubleValue> {
        val resultList = mutableListOf<YearMonthDoubleValue>()
        for (i in 1..12) {
            resultList.add(YearMonthDoubleValue(YearMonth.of(year, i), 0.0))
        }

        return resultList
    }
}
