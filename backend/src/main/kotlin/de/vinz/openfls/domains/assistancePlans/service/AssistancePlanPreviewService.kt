package de.vinz.openfls.domains.assistancePlans.service

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanExistingResponse
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanPreviewListResult
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanPeriodDto
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanPreviewResponse
import de.vinz.openfls.domains.assistancePlans.projection.AssistancePlanPreviewProjection
import de.vinz.openfls.domains.assistancePlans.projection.AssistancePlanWeeklyMinutesProjection
import de.vinz.openfls.domains.assistancePlans.repository.AssistancePlanPreviewRepository
import de.vinz.openfls.domains.clients.service.ClientService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.services.service.ServiceService
import de.vinz.openfls.services.TimeDoubleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class AssistancePlanPreviewService(
    private val assistancePlanPreviewRepository: AssistancePlanPreviewRepository,
    private val serviceService: ServiceService,
    private val clientService: ClientService,
    private val accessService: AccessService,
    private val clock: Clock
) {

    @Transactional(readOnly = true)
    fun getPreviewsOfCurrentUserByClientId(clientId: Long): List<AssistancePlanPreviewResponse>? {
        val client = clientService.getEntityById(clientId) ?: return null
        val includeArchived = accessService.isAdmin() || accessService.isLeader(client.institution?.id ?: 0)

        return getPreviewsByClientIdAndEmployeeId(clientId, accessService.getId(), includeArchived)
    }

    @Transactional(readOnly = true)
    fun getPreviewsByClientIdAndEmployeeId(
        clientId: Long,
        employeeId: Long,
        includeArchived: Boolean
    ): List<AssistancePlanPreviewResponse> {
        val previews = assistancePlanPreviewRepository.findPreviewProjectionsByClientId(clientId)
            .filter { includeArchived || !it.clientArchived }

        return createPreviewsWithFavorites(previews, employeeId)
    }

    @Transactional(readOnly = true)
    fun getPreviewsByInstitutionId(institutionId: Long): AssistancePlanPreviewListResult {
        if (!accessService.canReadEntries(institutionId)) {
            return AssistancePlanPreviewListResult.Forbidden
        }
        val includeArchived = accessService.isAdmin() || accessService.isLeader(institutionId)
        val previews = assistancePlanPreviewRepository.findPreviewProjectionsByInstitutionId(institutionId)
            .filter { includeArchived || !it.clientArchived }

        return AssistancePlanPreviewListResult.Success(createPreviewsWithFavorites(previews, accessService.getId()))
    }

    /** Only previews of institutions the signed-in employee may read; admins see all of them. */
    @Transactional(readOnly = true)
    fun getPreviewsBySponsorId(sponsorId: Long): List<AssistancePlanPreviewResponse> {
        val isAdmin = accessService.isAdmin()
        val readableInstitutionIds = if (isAdmin) emptySet() else accessService.getReadRightsInstitutionIds().toSet()
        val previews = assistancePlanPreviewRepository.findPreviewProjectionsBySponsorId(sponsorId)
            .filter { isAdmin || (!it.clientArchived && it.institutionId in readableInstitutionIds) }

        return createPreviewsWithFavorites(previews, accessService.getId())
    }

    @Transactional(readOnly = true)
    fun getFavoritePreviews(): List<AssistancePlanPreviewResponse> {
        val isAdmin = accessService.isAdmin()
        val leadingInstitutionIds = accessService.getLeadingInstitutionIds()
        val previews = assistancePlanPreviewRepository.findFavoritePreviewProjectionsByEmployeeId(accessService.getId())
            .filter { isAdmin || !it.clientArchived || leadingInstitutionIds.contains(it.institutionId) }

        return createPreviews(previews, previews.map { it.id }.toSet())
    }

    /**
     * Plan periods of several clients at once, used by overviews that only need to
     * know whether a client currently has a running assistance plan.
     */
    @Transactional(readOnly = true)
    fun getPeriodsByClientIds(clientIds: List<Long>): List<AssistancePlanPeriodDto> {
        if (clientIds.isEmpty()) {
            return emptyList()
        }
        return assistancePlanPreviewRepository.findPeriodDtosByClientIds(clientIds)
    }

    @Transactional(readOnly = true)
    fun getExistingByClientId(clientId: Long): List<AssistancePlanExistingResponse>? {
        val client = clientService.getEntityById(clientId) ?: return null
        val includeArchived = accessService.isAdmin() || accessService.isLeader(client.institution?.id ?: 0)

        return assistancePlanPreviewRepository.findExistingProjectionsByClientId(clientId)
            .filter { includeArchived || !it.clientArchived }
            .map { projection ->
                AssistancePlanExistingResponse(
                    id = projection.id,
                    start = projection.start,
                    end = projection.end,
                    sponsorName = projection.sponsorName,
                    clientArchived = projection.clientArchived
                )
            }
    }

    private fun createPreviewsWithFavorites(
        previews: List<AssistancePlanPreviewProjection>,
        employeeId: Long
    ): List<AssistancePlanPreviewResponse> {
        if (previews.isEmpty()) {
            return emptyList()
        }
        val favoriteAssistancePlanIds =
            assistancePlanPreviewRepository.findFavoriteAssistancePlanIdsByEmployeeId(employeeId).toSet()

        return createPreviews(previews, favoriteAssistancePlanIds)
    }

    private fun createPreviews(
        previews: List<AssistancePlanPreviewProjection>,
        favoriteAssistancePlanIds: Set<Long>
    ): List<AssistancePlanPreviewResponse> {
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
        val assistancePlanHourWeeklyMinutes = assistancePlanPreviewRepository
            .findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(assistancePlanIds)
        val goalHourWeeklyMinutes = assistancePlanPreviewRepository
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
    ): AssistancePlanPreviewResponse {
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

        return AssistancePlanPreviewResponse(
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
        return serviceService
            .getMinutesInPlanPeriodByAssistancePlanIdsAndStartAndEnd(assistancePlanIds, yearStart, yearEnd)
            .groupBy { it.assistancePlanId }
            .mapValues { (_, minutes) -> minutes.sumOf { it.minutes.toLong() } }
    }

    private fun getExecutedMinutesByAssistancePlanPeriodByAssistancePlanId(
        assistancePlanIds: List<Long>,
        periodEnd: LocalDate
    ): Map<Long, Long> {
        val minutesByAssistancePlanId = serviceService
            .getMinutesInPlanPeriodByAssistancePlanIdsUntil(assistancePlanIds, periodEnd)
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
