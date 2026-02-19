package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanPreviewDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanExistingDto
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanPreviewProjection
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanWeeklyMinutesProjection
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.services.TimeDoubleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class AssistancePlanPreviewService(
    private val assistancePlanRepository: AssistancePlanRepository,
    private val serviceRepository: ServiceRepository
) {

    @Transactional(readOnly = true)
    fun getPreviewDtosByClientId(clientId: Long, employeeId: Long): List<AssistancePlanPreviewDto> {
        val previews = assistancePlanRepository.findPreviewProjectionsByClientId(clientId)
        if (previews.isEmpty()) {
            return emptyList()
        }
        val favoriteAssistancePlanIds = assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(employeeId).toSet()
        return createPreviewDtos(previews, favoriteAssistancePlanIds)
    }

    @Transactional(readOnly = true)
    fun getPreviewDtosByInstitutionId(institutionId: Long, employeeId: Long): List<AssistancePlanPreviewDto> {
        val previews = assistancePlanRepository.findPreviewProjectionsByInstitutionId(institutionId)
        if (previews.isEmpty()) {
            return emptyList()
        }
        val favoriteAssistancePlanIds = assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(employeeId).toSet()
        return createPreviewDtos(previews, favoriteAssistancePlanIds)
    }

    @Transactional(readOnly = true)
    fun getPreviewDtosBySponsorId(sponsorId: Long, employeeId: Long): List<AssistancePlanPreviewDto> {
        val previews = assistancePlanRepository.findPreviewProjectionsBySponsorId(sponsorId)
        if (previews.isEmpty()) {
            return emptyList()
        }
        val favoriteAssistancePlanIds = assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(employeeId).toSet()
        return createPreviewDtos(previews, favoriteAssistancePlanIds)
    }

    @Transactional(readOnly = true)
    fun getFavoritePreviewDtosByEmployeeId(employeeId: Long): List<AssistancePlanPreviewDto> {
        val previews = assistancePlanRepository.findFavoritePreviewProjectionsByEmployeeId(employeeId)
        return createPreviewDtos(previews, previews.map { it.id }.toSet())
    }

    @Transactional(readOnly = true)
    fun getExistingDtosByClientId(clientId: Long): List<AssistancePlanExistingDto> {
        return assistancePlanRepository.findExistingProjectionsByClientId(clientId).map { projection ->
            AssistancePlanExistingDto(
                id = projection.id,
                start = projection.start,
                end = projection.end,
                sponsorName = projection.sponsorName
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
        val now = LocalDate.now()
        val yearStart = LocalDate.of(now.year, 1, 1)
        val assistancePlanIds = previews.map { it.id }

        return PreviewContext(
            now = now,
            yearStart = yearStart,
            periodEnd = now,
            weeklyApprovedMinutesByAssistancePlanId = getWeeklyApprovedMinutesByAssistancePlanId(assistancePlanIds),
            executedMinutesByAssistancePlanId = getExecutedMinutesByAssistancePlanId(assistancePlanIds, yearStart, now)
        )
    }

    private fun toPreviewDto(
        projection: AssistancePlanPreviewProjection,
        favoriteAssistancePlanIds: Set<Long>,
        context: PreviewContext
    ): AssistancePlanPreviewDto {
        val approvedWeeklyMinutes = context.weeklyApprovedMinutesByAssistancePlanId[projection.id] ?: 0.0
        val approvedHoursPerWeek = TimeDoubleService.convertDoubleToTimeDouble(approvedWeeklyMinutes / 60.0)
        val approvedHoursThisYear = TimeDoubleService.convertDoubleToTimeDouble(
            calculateApprovedHoursInYear(
                projection.start,
                projection.end,
                approvedWeeklyMinutes,
                context.yearStart,
                context.periodEnd
            )
        )
        val executedHoursThisYear = TimeDoubleService.convertDoubleToTimeDouble(
            (context.executedMinutesByAssistancePlanId[projection.id] ?: 0L) / 60.0
        )

        return AssistancePlanPreviewDto(
            id = projection.id,
            clientId = projection.clientId,
            institutionId = projection.institutionId,
            sponsorId = projection.sponsorId,
            clientFirstname = projection.clientFirstname,
            clientLastname = projection.clientLastname,
            institutionName = projection.institutionName,
            sponsorName = projection.sponsorName,
            start = projection.start,
            end = projection.end,
            isActive = isActiveOn(projection, context.now),
            isFavorite = favoriteAssistancePlanIds.contains(projection.id),
            approvedHoursPerWeek = approvedHoursPerWeek,
            approvedHoursThisYear = approvedHoursThisYear,
            executedHoursThisYear = executedHoursThisYear
        )
    }

    private fun isActiveOn(projection: AssistancePlanPreviewProjection, date: LocalDate): Boolean {
        return projection.start <= date && projection.end >= date
    }

    private fun getWeeklyApprovedMinutesByAssistancePlanId(assistancePlanIds: List<Long>): Map<Long, Double> {
        val assistancePlanHourMinutes = assistancePlanRepository
            .findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(assistancePlanIds)
            .sumWeeklyMinutesByAssistancePlanId()

        val goalHourMinutes = assistancePlanRepository
            .findWeeklyMinutesFromGoalHoursByAssistancePlanIds(assistancePlanIds)
            .sumWeeklyMinutesByAssistancePlanId()

        return assistancePlanIds.associateWith { assistancePlanId ->
            (assistancePlanHourMinutes[assistancePlanId] ?: 0.0) + (goalHourMinutes[assistancePlanId] ?: 0.0)
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
        val executedMinutesByAssistancePlanId: Map<Long, Long>
    )
}
