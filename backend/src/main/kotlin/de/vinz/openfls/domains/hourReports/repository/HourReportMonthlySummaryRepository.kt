package de.vinz.openfls.domains.hourReports.repository

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummaryProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import java.time.LocalDate

/** Read model of the monthly analysis: plans overlapping a period together with their hours and goals. */
interface HourReportMonthlySummaryRepository : Repository<AssistancePlan, Long> {

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE :end >= u.start AND :start <= u.end")
    fun findSummaryProjectionsByPeriod(
        start: LocalDate,
        end: LocalDate
    ): List<HourReportMonthlySummaryProjection>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.sponsor.id = :sponsorId " +
            "AND (:end >= u.start AND :start <= u.end)")
    fun findSummaryProjectionsBySponsorIdAndPeriod(
        sponsorId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<HourReportMonthlySummaryProjection>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId " +
            "AND (:end >= u.start AND :start <= u.end)")
    fun findSummaryProjectionsByInstitutionIdAndPeriod(
        institutionId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<HourReportMonthlySummaryProjection>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId " +
            "And u.sponsor.id = :sponsorId " +
            "AND (:end >= u.start AND :start <= u.end)")
    fun findSummaryProjectionsByInstitutionIdAndSponsorIdAndPeriod(
        institutionId: Long,
        sponsorId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<HourReportMonthlySummaryProjection>
}
