package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanExistingDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanPreviewDto
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanPreviewProjection
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanWeeklyMinutesProjection
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.services.TimeDoubleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class AssistancePlanPreviewService(
    private val assistancePlanRepository: AssistancePlanRepository,
    private val serviceRepository: ServiceRepository,
    private val clock: Clock
) {

    @Transactional(readOnly = true)
    fun getPreviewDtosByClientId(
        clientId: Long,
        employeeId: Long,
        includeArchived: Boolean = false
    ): List<AssistancePlanPreviewDto> {
        val previews = assistancePlanRepository.findPreviewProjectionsByClientId(clientId)
            .filter { includeArchived || !it.clientArchived }
        if (previews.isEmpty()) {
            return emptyList()
        }
        val favoriteAssistancePlanIds = assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(employeeId).toSet()
        return createPreviewDtos(previews, favoriteAssistancePlanIds)
    }

    @Transactional(readOnly = true)
    fun getPreviewDtosByInstitutionId(
        institutionId: Long,
        employeeId: Long,
        includeArchived: Boolean = false
    ): List<AssistancePlanPreviewDto> {
        val previews = assistancePlanRepository.findPreviewProjectionsByInstitutionId(institutionId)
            .filter { includeArchived || !it.clientArchived }
        if (previews.isEmpty()) {
            return emptyList()
        }
        val favoriteAssistancePlanIds = assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(employeeId).toSet()
        return createPreviewDtos(previews, favoriteAssistancePlanIds)
    }

    @Transactional(readOnly = true)
    fun getPreviewDtosBySponsorId(
        sponsorId: Long,
        employeeId: Long,
        includeArchived: Boolean = false
    ): List<AssistancePlanPreviewDto> {
        val previews = assistancePlanRepository.findPreviewProjectionsBySponsorId(sponsorId)
            .filter { includeArchived || !it.clientArchived }
        if (previews.isEmpty()) {
            return emptyList()
        }
        val favoriteAssistancePlanIds = assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(employeeId).toSet()
        return createPreviewDtos(previews, favoriteAssistancePlanIds)
    }

    @Transactional(readOnly = true)
    fun getFavoritePreviewDtosByEmployeeId(
        employeeId: Long,
        includeArchived: Boolean = false,
        leadingInstitutionIds: List<Long> = emptyList()
    ): List<AssistancePlanPreviewDto> {
        val previews = assistancePlanRepository.findFavoritePreviewProjectionsByEmployeeId(employeeId)
            .filter { includeArchived || !it.clientArchived || leadingInstitutionIds.contains(it.institutionId) }
        return createPreviewDtos(previews, previews.map { it.id }.toSet())
    }

    @Transactional(readOnly = true)
    fun getExistingDtosByClientId(
        clientId: Long,
        includeArchived: Boolean = false
    ): List<AssistancePlanExistingDto> {
        return assistancePlanRepository.findExistingProjectionsByClientId(clientId)
            .filter { includeArchived || !it.clientArchived }
            .map { projection ->
                AssistancePlanExistingDto(
                    id = projection.id,
                    start = projection.start,
                    end = projection.end,
                    sponsorName = projection.sponsorName,
                    clientArchived = projection.clientArchived
                )
            }
    }

    private fun createPreviewDtos(
        previews: List<AssistancePlanPreviewProjection>,
        favoriteAssistancePlanIds: Set<Long>
    ): List<AssistancePlanPreviewDto> {
        if (previews.isEmpty()) {
            return emptyList()
        }

        val context = buildPreviewContext(previews)
        return previews.map { projection -> toPreviewDto(projection, favoriteAssistancePlanIds, context) }
    }

    private fun buildPreviewContext(previews: List<AssistancePlanPreviewProjection>): PreviewContext {
        val now = LocalDate.now(clock)
        val yearStart = LocalDate.of(now.year, 1, 1)
        val assistancePlanIds = previews.map { it.id }
        val assistancePlanHourWeeklyMinutes = assistancePlanRepository
            .findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(assistancePlanIds)
        val goalHourWeeklyMinutes = assistancePlanRepository
            .findWeeklyMinutesFromGoalHoursByAssistancePlanIds(assistancePlanIds)

        val executedMinutesByAssistancePlanId = getExecutedMinutesByAssistancePlanId(assistancePlanIds, yearStart, now)

        return PreviewContext(
            now = now,
            yearStart = yearStart,
            periodEnd = now,
            weeklyApprovedMinutesByAssistancePlanId = getWeeklyApprovedMinutesByAssistancePlanId(
                assistancePlanIds,
                assistancePlanHourWeeklyMinutes,
                goalHourWeeklyMinutes
            ),
            assistancePlanIdsWithPlanHours = assistancePlanHourWeeklyMinutes.map { it.assistancePlanId }.toSet(),
            assistancePlanIdsWithGoalHours = goalHourWeeklyMinutes.map { it.assistancePlanId }.toSet(),
            executedMinutesByAssistancePlanId = executedMinutesByAssistancePlanId,
            executedMinutesByAssistancePlanPeriodByAssistancePlanId = getExecutedMinutesByAssistancePlanPeriodByAssistancePlanId(
                assistancePlanIds,
                now
            )
        )
    }

    private fun toPreviewDto(
        projection: AssistancePlanPreviewProjection,
        favoriteAssistancePlanIds: Set<Long>,
        context: PreviewContext
    ): AssistancePlanPreviewDto {
        val approvedRangeMinutes = if (projection.hourMode == AssistancePlanHourMode.CORRIDOR) {
            val from = projection.hourCorridorWeeklyMinutesFrom?.toDouble() ?: 0.0
            val till = projection.hourCorridorWeeklyMinutesTill?.toDouble() ?: from
            from to till
        } else {
            val approvedWeeklyMinutes = context.weeklyApprovedMinutesByAssistancePlanId[projection.id] ?: 0.0
            approvedWeeklyMinutes to approvedWeeklyMinutes
        }
        val approvedHoursFrom = TimeDoubleService.convertDoubleToTimeDouble(approvedRangeMinutes.first / 60.0)
        val approvedHoursTo = TimeDoubleService.convertDoubleToTimeDouble(approvedRangeMinutes.second / 60.0)
        val approvedHoursPerWeek = TimeDoubleService.convertDoubleToTimeDouble(
            ((approvedRangeMinutes.first + approvedRangeMinutes.second) / 2.0) / 60.0
        )
        val approvedHoursThisYearFrom = TimeDoubleService.convertDoubleToTimeDouble(
            calculateApprovedHoursInYear(
                projection.start,
                projection.end,
                approvedRangeMinutes.first,
                context.yearStart,
                context.periodEnd
            )
        )
        val approvedHoursThisYearTill = TimeDoubleService.convertDoubleToTimeDouble(
            calculateApprovedHoursInYear(
                projection.start,
                projection.end,
                approvedRangeMinutes.second,
                context.yearStart,
                context.periodEnd
            )
        )
        val approvedHoursThisYear = TimeDoubleService.convertDoubleToTimeDouble(
            calculateApprovedHoursInYear(
                projection.start,
                projection.end,
                (approvedRangeMinutes.first + approvedRangeMinutes.second) / 2.0,
                context.yearStart,
                context.periodEnd
            )
        )
        val executedHoursThisYear = TimeDoubleService.convertDoubleToTimeDouble(
            (context.executedMinutesByAssistancePlanId[projection.id] ?: 0L) / 60.0
        )
        val approvedHoursLeftThisYear = calculateApprovedHoursLeftThisYear(
            approvedHoursThisYearFrom,
            approvedHoursThisYearTill,
            executedHoursThisYear
        )
        val approvedHoursThisAssistancePlanFrom = TimeDoubleService.convertDoubleToTimeDouble(
            calculateApprovedHoursInYear(
                projection.start,
                projection.end,
                approvedRangeMinutes.first,
                projection.start,
                context.periodEnd
            )
        )
        val approvedHoursThisAssistancePlanTill = TimeDoubleService.convertDoubleToTimeDouble(
            calculateApprovedHoursInYear(
                projection.start,
                projection.end,
                approvedRangeMinutes.second,
                projection.start,
                context.periodEnd
            )
        )
        val approvedHoursThisAssistancePlan = TimeDoubleService.convertDoubleToTimeDouble(
            calculateApprovedHoursInYear(
                projection.start,
                projection.end,
                (approvedRangeMinutes.first + approvedRangeMinutes.second) / 2.0,
                projection.start,
                context.periodEnd
            )
        )
        val executedHoursThisAssistancePlan = TimeDoubleService.convertDoubleToTimeDouble(
            (context.executedMinutesByAssistancePlanPeriodByAssistancePlanId[projection.id] ?: 0L) / 60.0
        )
        val approvedHoursLeftThisAssistancePlan = calculateApprovedHoursLeftThisYear(
            approvedHoursThisAssistancePlanFrom,
            approvedHoursThisAssistancePlanTill,
            executedHoursThisAssistancePlan
        )

        return AssistancePlanPreviewDto(
            id = projection.id,
            clientId = projection.clientId,
            institutionId = projection.institutionId,
            sponsorId = projection.sponsorId,
            clientFirstname = projection.clientFirstname,
            clientLastname = projection.clientLastname,
            clientArchived = projection.clientArchived,
            institutionName = projection.institutionName,
            sponsorName = projection.sponsorName,
            start = projection.start,
            end = projection.end,
            isActive = isActiveOn(projection, context.now),
            isFavorite = favoriteAssistancePlanIds.contains(projection.id),
            hasIllegalHours = hasIllegalHours(projection, context),
            hourMode = projection.hourMode,
            approvedHoursFrom = approvedHoursFrom,
            approvedHoursTo = approvedHoursTo,
            approvedHoursPerWeek = approvedHoursPerWeek,
            approvedHoursThisYearFrom = approvedHoursThisYearFrom,
            approvedHoursThisYearTill = approvedHoursThisYearTill,
            approvedHoursThisYear = approvedHoursThisYear,
            executedHoursThisYear = executedHoursThisYear,
            approvedHoursLeftThisYear = approvedHoursLeftThisYear,
            approvedHoursThisAssistancePlanFrom = approvedHoursThisAssistancePlanFrom,
            approvedHoursThisAssistancePlanTill = approvedHoursThisAssistancePlanTill,
            approvedHoursThisAssistancePlan = approvedHoursThisAssistancePlan,
            executedHoursThisAssistancePlan = executedHoursThisAssistancePlan,
            approvedHoursLeftThisAssistancePlan = approvedHoursLeftThisAssistancePlan
        )
    }

    private fun calculateApprovedHoursLeftThisYear(
        approvedHoursThisYearFrom: Double,
        approvedHoursThisYearTill: Double,
        executedHoursThisYear: Double
    ): Double {
        val approvedFrom = TimeDoubleService.convertTimeDoubleToDouble(approvedHoursThisYearFrom)
        val approvedTill = TimeDoubleService.convertTimeDoubleToDouble(approvedHoursThisYearTill)
        val executed = TimeDoubleService.convertTimeDoubleToDouble(executedHoursThisYear)

        return when {
            executed < approvedFrom ->
                TimeDoubleService.diffTimeDoubles(approvedHoursThisYearFrom, executedHoursThisYear)
            executed >= approvedTill ->
                TimeDoubleService.diffTimeDoubles(approvedHoursThisYearTill, executedHoursThisYear)
            else -> 0.0
        }
    }

    private fun isActiveOn(projection: AssistancePlanPreviewProjection, date: LocalDate): Boolean {
        return projection.start <= date && projection.end >= date
    }

    private fun getWeeklyApprovedMinutesByAssistancePlanId(
        assistancePlanIds: List<Long>,
        assistancePlanHourWeeklyMinutes: List<AssistancePlanWeeklyMinutesProjection>,
        goalHourWeeklyMinutes: List<AssistancePlanWeeklyMinutesProjection>
    ): Map<Long, Double> {
        val assistancePlanHourMinutes = assistancePlanHourWeeklyMinutes.sumWeeklyMinutesByAssistancePlanId()
        val goalHourMinutes = goalHourWeeklyMinutes.sumWeeklyMinutesByAssistancePlanId()
        val assistancePlanIdsWithPlanHours = assistancePlanHourWeeklyMinutes
            .map { it.assistancePlanId }
            .toSet()

        return assistancePlanIds.associateWith { assistancePlanId ->
            if (assistancePlanIdsWithPlanHours.contains(assistancePlanId)) {
                assistancePlanHourMinutes[assistancePlanId] ?: 0.0
            } else {
                goalHourMinutes[assistancePlanId] ?: 0.0
            }
        }
    }

    private fun getExecutedMinutesByAssistancePlanId(
        assistancePlanIds: List<Long>,
        yearStart: LocalDate,
        yearEnd: LocalDate
    ): Map<Long, Long> {
        return serviceRepository
            .findMinutesByAssistancePlanIdsAndStartAndEnd(assistancePlanIds, yearStart, yearEnd)
            .groupBy { it.assistancePlanId }
            .mapValues { (_, minutes) -> minutes.sumOf { it.minutes.toLong() } }
    }

    private fun getExecutedMinutesByAssistancePlanPeriodByAssistancePlanId(
        assistancePlanIds: List<Long>,
        periodEnd: LocalDate
    ): Map<Long, Long> {
        val minutesByAssistancePlanId = serviceRepository
            .findMinutesByAssistancePlanIdsFromPlanStartToEnd(assistancePlanIds, periodEnd)
            .groupBy { it.assistancePlanId }
            .mapValues { (_, minutes) -> minutes.sumOf { it.minutes.toLong() } }
        return assistancePlanIds.associateWith { minutesByAssistancePlanId[it] ?: 0L }
    }

    private fun calculateApprovedHoursInYear(
        start: LocalDate,
        end: LocalDate,
        approvedWeeklyMinutes: Double,
        yearStart: LocalDate,
        yearEnd: LocalDate
    ): Double {
        val overlapStart = if (start.isAfter(yearStart)) start else yearStart
        val overlapEnd = if (end.isBefore(yearEnd)) end else yearEnd
        if (overlapEnd.isBefore(overlapStart)) {
            return 0.0
        }

        val daysInYearOverlap = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1
        return (daysInYearOverlap * (approvedWeeklyMinutes / 7.0)) / 60.0
    }

    private fun List<AssistancePlanWeeklyMinutesProjection>.sumWeeklyMinutesByAssistancePlanId(): Map<Long, Double> {
        return this.groupBy { it.assistancePlanId }
            .mapValues { (_, minutes) -> minutes.sumOf { it.weeklyMinutes.toDouble() } }
    }

    private data class PreviewContext(
        val now: LocalDate,
        val yearStart: LocalDate,
        val periodEnd: LocalDate,
        val weeklyApprovedMinutesByAssistancePlanId: Map<Long, Double>,
        val assistancePlanIdsWithPlanHours: Set<Long>,
        val assistancePlanIdsWithGoalHours: Set<Long>,
        val executedMinutesByAssistancePlanId: Map<Long, Long>,
        val executedMinutesByAssistancePlanPeriodByAssistancePlanId: Map<Long, Long>
    )

    private fun hasIllegalHours(
        projection: AssistancePlanPreviewProjection,
        context: PreviewContext
    ): Boolean {
        val hasPlanHours = context.assistancePlanIdsWithPlanHours.contains(projection.id)
        val hasGoalHours = context.assistancePlanIdsWithGoalHours.contains(projection.id)

        return when (projection.hourMode) {
            AssistancePlanHourMode.CORRIDOR -> hasPlanHours || hasGoalHours
            AssistancePlanHourMode.EXACT -> hasPlanHours && hasGoalHours || (!hasPlanHours && !hasGoalHours)
        }
    }
}
