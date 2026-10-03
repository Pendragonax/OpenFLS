package de.vinz.openfls.domains.hourReports.service

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.hourReports.dto.HourReportMonthlySummaryResult
import de.vinz.openfls.domains.hourReports.repository.HourReportMonthlySummaryRepository
import de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummaryProjection
import de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummaryClientProjection
import de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummaryGoalProjection
import de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummaryHourCorridorProjection
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummaryHourTypeProjection
import de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummaryInstitutionProjection
import de.vinz.openfls.domains.services.dto.ServiceDto
import de.vinz.openfls.domains.services.service.ServiceService
import de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummarySponsorProjection
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

class HourReportMonthlySummaryServiceTest {

    private val hourReportMonthlySummaryRepository: HourReportMonthlySummaryRepository = mock()
    private val serviceService: ServiceService = mock()
    private val accessService: de.vinz.openfls.domains.permissions.service.AccessService = mock()
    private val summaryService = HourReportMonthlySummaryService(
        hourReportMonthlySummaryRepository,
        serviceService,
        accessService
    )

    @Test
    fun getMonthlySummary_corridorPlan_marksKorAndUsesAverageApprovedHours() {
        // Given
        val today = LocalDate.now()
        val planStart = today.withDayOfMonth(1)
        val planEnd = today.withDayOfMonth(today.lengthOfMonth())
        val monthDays = DateService.countDaysOfMonthAndYearBetweenStartAndEnd(
            today.year,
            today.monthValue,
            planStart,
            planEnd
        )
        val hourType = HourType(id = 1, title = "Korridor")
        val projection = corridorProjection(planStart, planEnd, hourType)
        val executedMinutes = 3000

        whenever(
            serviceService.getServicesByAssistancePlanIdAndYearAndMonth(
                assistancePlanId = projection.id,
                year = today.year,
                month = today.monthValue
            )
        ).thenReturn(listOf(serviceProjection(projection.id, executedMinutes)))
        whenever(accessService.canReadEntries(0)).thenReturn(true)
        whenever(hourReportMonthlySummaryRepository.findSummaryProjectionsByPeriod(planStart, planEnd))
            .thenReturn(listOf(projection))

        // When
        val result = summaryService.getMonthlySummary(today.year, today.monthValue, 0, 0, 0)

        // Then
        val rows = (result as HourReportMonthlySummaryResult.Success).response.rows
        assertThat(rows).hasSize(1)
        val entry = rows.first()
        val expectedApprovedHours = TimeDoubleService.convertDoubleToTimeDouble((monthDays * 7.5) / 7.0)
        val expectedMonthlyTo = TimeDoubleService.convertDoubleToTimeDouble((monthDays * 10.0) / 7.0)

        assertThat(entry.approvedHours).isEqualTo(expectedApprovedHours)
        assertThat(entry.clientLastName).isEqualTo("Muster")
        assertThat(entry.hourMode).isEqualTo(AssistancePlanHourMode.CORRIDOR)
        assertThat(entry.missingHours).isNegative()
        assertThat(entry.executedPercent).isEqualTo(
            TimeDoubleService.roundDoubleToTwoDigits((executedMinutes / 60.0) * 100 / expectedMonthlyTo)
        )
    }

    @Test
    fun getMonthlySummary_corridorPlanWithHourType_usesMonthlyUpperBoundForUtilization() {
        // Given
        val today = LocalDate.now()
        val planStart = today.withDayOfMonth(1)
        val planEnd = today.withDayOfMonth(today.lengthOfMonth())
        val hourType = HourType(id = 2, title = "Korridor")
        val projection = corridorProjection(planStart, planEnd, hourType)
        val executedMinutes = 3000

        whenever(
            serviceService.getServicesByAssistancePlanIdAndHourTypeIdAndYearAndMonth(
                assistancePlanId = projection.id,
                hourTypeId = hourType.id,
                year = today.year,
                month = today.monthValue
            )
        ).thenReturn(listOf(serviceProjection(projection.id, executedMinutes)))

        whenever(accessService.canReadEntries(0)).thenReturn(true)
        whenever(accessService.isAdmin()).thenReturn(true)
        whenever(hourReportMonthlySummaryRepository.findSummaryProjectionsByPeriod(planStart, planEnd))
            .thenReturn(listOf(projection))

        // When
        val summary = summaryService.getMonthlySummary(today.year, today.monthValue, 0, 0, hourType.id)

        // Then
        val result = (summary as HourReportMonthlySummaryResult.Success).response.rows.single()
        val monthDays = DateService.countDaysOfMonthAndYearBetweenStartAndEnd(
            today.year,
            today.monthValue,
            planStart,
            planEnd
        )
        val expectedApprovedHours = TimeDoubleService.convertDoubleToTimeDouble((monthDays * 7.5) / 7.0)
        val expectedMonthlyFrom = TimeDoubleService.convertDoubleToTimeDouble((monthDays * 5.0) / 7.0)
        val expectedMonthlyTo = TimeDoubleService.convertDoubleToTimeDouble((monthDays * 10.0) / 7.0)

        assertThat(result.approvedHours).isEqualTo(expectedApprovedHours)
        assertThat(result.clientLastName).isEqualTo("Muster")
        assertThat(result.missingHours).isEqualTo(
            TimeDoubleService.diffTimeDoubles(expectedMonthlyTo, TimeDoubleService.convertDoubleToTimeDouble(executedMinutes / 60.0))
        )
        assertThat(result.executedPercent).isEqualTo(
            TimeDoubleService.roundDoubleToTwoDigits((executedMinutes / 60.0) * 100 / expectedMonthlyTo)
        )
    }

    @Test
    fun getMonthlySummary_invalidMonth_returnsInvalidPeriod() {
        // Given
        whenever(accessService.canReadEntries(0)).thenReturn(true)

        // When / Then
        assertThat(summaryService.getMonthlySummary(2026, 13, 0, 0, 0))
            .isEqualTo(HourReportMonthlySummaryResult.InvalidPeriod)
        assertThat(summaryService.getMonthlySummary(-1, 5, 0, 0, 0))
            .isEqualTo(HourReportMonthlySummaryResult.InvalidPeriod)
    }

    @Test
    fun getMonthlySummary_institutionWithoutReadRights_returnsForbidden() {
        // Given
        whenever(accessService.canReadEntries(4)).thenReturn(false)

        // When / Then
        assertThat(summaryService.getMonthlySummary(2026, 5, 4, 0, 0))
            .isEqualTo(HourReportMonthlySummaryResult.Forbidden)
    }

    @Test
    fun getMonthlySummary_allInstitutionsWithHourTypeAsNonAdmin_returnsForbidden() {
        // Given
        whenever(accessService.canReadEntries(0)).thenReturn(true)
        whenever(accessService.isAdmin()).thenReturn(false)

        // When / Then
        assertThat(summaryService.getMonthlySummary(2026, 5, 0, 0, 3))
            .isEqualTo(HourReportMonthlySummaryResult.Forbidden)
    }

    private fun corridorProjection(start: LocalDate, end: LocalDate, hourType: HourType): HourReportMonthlySummaryProjection {
        val corridorHourType = object : HourReportMonthlySummaryHourTypeProjection {
            override val id: Long = hourType.id
            override val title: String = hourType.title
            override val price: Double = hourType.price
        }
        val corridor = object : HourReportMonthlySummaryHourCorridorProjection {
            override val id: Long = 5
            override val title: String = "5 bis 10"
            override val weeklyMinutesFrom: Int = 300
            override val weeklyMinutesTill: Int = 600
            override val hourType: HourReportMonthlySummaryHourTypeProjection = corridorHourType
        }

        return object : HourReportMonthlySummaryProjection {
            override val id: Long = 5
            override val start: LocalDate = start
            override val end: LocalDate = end
            override val client: HourReportMonthlySummaryClientProjection = clientProjection()
            override val sponsor: HourReportMonthlySummarySponsorProjection = sponsorProjection()
            override val institution: HourReportMonthlySummaryInstitutionProjection = institutionProjection()
            override val hourMode: AssistancePlanHourMode = AssistancePlanHourMode.CORRIDOR
            override val hourCorridor: HourReportMonthlySummaryHourCorridorProjection? = corridor
            override val hours: List<de.vinz.openfls.domains.hourReports.projection.HourReportMonthlySummaryHourProjection> = emptyList()
            override val goals: List<HourReportMonthlySummaryGoalProjection> = emptyList()
        }
    }

    private fun clientProjection(): HourReportMonthlySummaryClientProjection {
        return object : HourReportMonthlySummaryClientProjection {
            override val id: Long = 1
            override val firstName: String = "Max"
            override val lastName: String = "Muster"
            override val phoneNumber: String = ""
            override val email: String = ""
            override val archived: Boolean = false
        }
    }

    private fun sponsorProjection(): HourReportMonthlySummarySponsorProjection {
        return object : HourReportMonthlySummarySponsorProjection {
            override val id: Long = 1
            override val name: String = "Sponsor"
            override val payOverhang: Boolean = true
            override val payExact: Boolean = false
        }
    }

    private fun institutionProjection(): HourReportMonthlySummaryInstitutionProjection {
        return object : HourReportMonthlySummaryInstitutionProjection {
            override val id: Long = 1
            override val name: String = "Institution"
            override val email: String = ""
            override val phonenumber: String = ""
        }
    }

    private fun serviceProjection(assistancePlanId: Long, minutes: Int): ServiceDto {
        val start = LocalDate.now().atStartOfDay()
        return ServiceDto(
            id = 1,
            start = start,
            end = start.plusMinutes(minutes.toLong()),
            minutes = minutes,
            title = "",
            content = "",
            unfinished = false,
            groupService = false
        )
    }
}
