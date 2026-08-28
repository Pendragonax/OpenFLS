package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanPreviewProjection
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanExistingProjection
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanWeeklyMinutesProjection
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.domains.services.projections.AssistancePlanServiceMinutesProjection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import de.vinz.openfls.services.TimeDoubleService
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.stream.Stream
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@ExtendWith(MockitoExtension::class)
class AssistancePlanPreviewServiceTest {

    @Mock
    lateinit var assistancePlanRepository: AssistancePlanRepository

    @Mock
    lateinit var serviceRepository: ServiceRepository

    private val clock = Clock.fixed(Instant.parse("2026-07-01T12:00:00Z"), ZoneOffset.UTC)
    private lateinit var previewService: AssistancePlanPreviewService

    @BeforeEach
    fun setUp() {
        previewService = AssistancePlanPreviewService(assistancePlanRepository, serviceRepository, clock)
    }

    @Test
    fun getPreviewDtosByClientId_calculatesApprovedAndExecutedHoursInKotlin() {
        val now = LocalDate.now(clock)
        val yearStart = LocalDate.of(now.year, 1, 1)
        val periodEnd = now

        val projection = previewProjection(5L, yearStart, now.plusDays(30), false)

        whenever(assistancePlanRepository.findPreviewProjectionsByClientId(10L))
            .thenReturn(listOf(projection))
        whenever(assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(20L))
            .thenReturn(listOf(5L))
        whenever(assistancePlanRepository.findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(5L)))
            .thenReturn(listOf(weeklyMinutesProjection(5L, 120)))
        whenever(assistancePlanRepository.findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(5L)))
            .thenReturn(listOf(weeklyMinutesProjection(5L, 300)))
        whenever(serviceRepository.findMinutesByAssistancePlanIdsFromPlanStartToEnd(listOf(5L), periodEnd))
            .thenReturn(
                listOf(
                    serviceMinutesProjection(5L, 120),
                    serviceMinutesProjection(5L, 60)
                )
            )
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(5L), yearStart, periodEnd))
            .thenReturn(listOf(serviceMinutesProjection(5L, 120), serviceMinutesProjection(5L, 60)))

        val result = previewService.getPreviewDtosByClientId(10L, 20L)

        assertThat(result).hasSize(1)
        assertThat(result.first().id).isEqualTo(5L)
        assertThat(result.first().isFavorite).isTrue()
        assertThat(result.first().hasIllegalHours).isTrue()
        assertThat(result.first().hourMode).isEqualTo(AssistancePlanHourMode.EXACT)
        assertThat(result.first().approvedHoursFrom).isEqualTo(2.0)
        assertThat(result.first().approvedHoursTo).isEqualTo(2.0)
        assertThat(result.first().approvedHoursPerWeek).isEqualTo(2.0)
        assertThat(result.first().approvedHoursThisYearFrom)
            .isEqualTo(expectedApprovedHoursThisYear(120.0, yearStart, now.plusDays(30), yearStart, now))
        assertThat(result.first().approvedHoursThisYearTill)
            .isEqualTo(expectedApprovedHoursThisYear(120.0, yearStart, now.plusDays(30), yearStart, now))
        assertThat(result.first().executedHoursThisYear).isEqualTo(3.0)
        assertThat(result.first().approvedHoursThisYear)
            .isEqualTo(expectedApprovedHoursThisYear(120.0, yearStart, now.plusDays(30), yearStart, now))
        assertThat(result.first().approvedHoursLeftThisYear).isEqualTo(
            TimeDoubleService.diffTimeDoubles(result.first().approvedHoursThisYearFrom, result.first().executedHoursThisYear)
        )
        assertThat(result.first().approvedHoursThisAssistancePlanFrom)
            .isEqualTo(result.first().approvedHoursThisYearFrom)
        assertThat(result.first().approvedHoursThisAssistancePlanTill)
            .isEqualTo(result.first().approvedHoursThisYearTill)
        assertThat(result.first().approvedHoursThisAssistancePlan)
            .isEqualTo(result.first().approvedHoursThisYear)
        assertThat(result.first().executedHoursThisAssistancePlan)
            .isEqualTo(result.first().executedHoursThisYear)
        assertThat(result.first().approvedHoursLeftThisAssistancePlan)
            .isEqualTo(result.first().approvedHoursLeftThisYear)
        verify(serviceRepository).findMinutesByAssistancePlanIdsFromPlanStartToEnd(listOf(5L), now)
    }

    @Test
    fun getPreviewDtosByInstitutionId_noPreviews_skipsFurtherQueries() {
        whenever(assistancePlanRepository.findPreviewProjectionsByInstitutionId(11L))
            .thenReturn(emptyList())

        val result = previewService.getPreviewDtosByInstitutionId(11L, 22L)

        assertThat(result).isEmpty()
        verify(assistancePlanRepository, never()).findFavoriteAssistancePlanIdsByEmployeeId(any())
        verify(assistancePlanRepository, never()).findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(any())
        verify(assistancePlanRepository, never()).findWeeklyMinutesFromGoalHoursByAssistancePlanIds(any())
        verify(serviceRepository, never()).findMinutesByAssistancePlanIdsFromPlanStartToEnd(any(), any())
    }

    @Test
    fun getPreviewDtosByClientId_filtersArchivedPlansUnlessIncluded() {
        val now = LocalDate.now(clock)
        val yearStart = LocalDate.of(now.year, 1, 1)
        val periodEnd = now
        val projection = previewProjection(6L, now.minusDays(10), now.plusDays(10), true)

        whenever(assistancePlanRepository.findPreviewProjectionsByClientId(10L))
            .thenReturn(listOf(projection))
        whenever(assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(20L))
            .thenReturn(listOf(6L))
        whenever(assistancePlanRepository.findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(6L)))
            .thenReturn(listOf(weeklyMinutesProjection(6L, 180)))
        whenever(assistancePlanRepository.findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(6L)))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsFromPlanStartToEnd(listOf(6L), periodEnd))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(6L), yearStart, periodEnd))
            .thenReturn(emptyList())

        val hiddenResult = previewService.getPreviewDtosByClientId(10L, 20L)
        val visibleResult = previewService.getPreviewDtosByClientId(10L, 20L, includeArchived = true)

        assertThat(hiddenResult).isEmpty()
        assertThat(visibleResult).hasSize(1)
        assertThat(visibleResult.first().clientArchived).isTrue()
    }

    @Test
    fun getFavoritePreviewDtosByEmployeeId_marksAllReturnedPlansAsFavorite() {
        val now = LocalDate.now(clock)
        val yearStart = LocalDate.of(now.year, 1, 1)
        val periodEnd = now

        val projection = previewProjection(7L, now.minusDays(10), now.plusDays(10), false)

        whenever(assistancePlanRepository.findFavoritePreviewProjectionsByEmployeeId(33L))
            .thenReturn(listOf(projection))
        whenever(assistancePlanRepository.findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(7L)))
            .thenReturn(listOf(weeklyMinutesProjection(7L, 210)))
        whenever(assistancePlanRepository.findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(7L)))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsFromPlanStartToEnd(listOf(7L), periodEnd))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(7L), yearStart, periodEnd))
            .thenReturn(emptyList())

        val result = previewService.getFavoritePreviewDtosByEmployeeId(33L)

        assertThat(result).hasSize(1)
        assertThat(result.first().isFavorite).isTrue()
        assertThat(result.first().isActive).isTrue()
        assertThat(result.first().hasIllegalHours).isFalse()
        assertThat(result.first().approvedHoursPerWeek).isEqualTo(3.3)
        assertThat(result.first().executedHoursThisYear).isEqualTo(0.0)
    }

    @Test
    fun getFavoritePreviewDtosByEmployeeId_filtersArchivedPlansForLeads() {
        val now = LocalDate.now(clock)
        val yearStart = LocalDate.of(now.year, 1, 1)
        val periodEnd = now
        val projection = previewProjection(8L, now.minusDays(10), now.plusDays(10), true)

        whenever(assistancePlanRepository.findFavoritePreviewProjectionsByEmployeeId(44L))
            .thenReturn(listOf(projection))
        whenever(assistancePlanRepository.findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(8L)))
            .thenReturn(listOf(weeklyMinutesProjection(8L, 180)))
        whenever(assistancePlanRepository.findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(8L)))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsFromPlanStartToEnd(listOf(8L), periodEnd))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(8L), yearStart, periodEnd))
            .thenReturn(emptyList())

        val hiddenResult = previewService.getFavoritePreviewDtosByEmployeeId(44L)
        val visibleResult = previewService.getFavoritePreviewDtosByEmployeeId(
            44L,
            includeArchived = false,
            leadingInstitutionIds = listOf(102L)
        )

        assertThat(hiddenResult).isEmpty()
        assertThat(visibleResult).hasSize(1)
        assertThat(visibleResult.first().clientArchived).isTrue()
    }

    @Test
    fun getPreviewDtosBySponsorId_filtersArchivedPlansUnlessIncluded() {
        val now = LocalDate.now(clock)
        val yearStart = LocalDate.of(now.year, 1, 1)
        val periodEnd = now
        val projection = previewProjection(9L, now.minusDays(10), now.plusDays(10), true)

        whenever(assistancePlanRepository.findPreviewProjectionsBySponsorId(77L))
            .thenReturn(listOf(projection))
        whenever(assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(55L))
            .thenReturn(listOf(9L))
        whenever(assistancePlanRepository.findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(9L)))
            .thenReturn(listOf(weeklyMinutesProjection(9L, 240)))
        whenever(assistancePlanRepository.findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(9L)))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsFromPlanStartToEnd(listOf(9L), periodEnd))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(9L), yearStart, periodEnd))
            .thenReturn(emptyList())

        val hiddenResult = previewService.getPreviewDtosBySponsorId(77L, 55L)
        val visibleResult = previewService.getPreviewDtosBySponsorId(77L, 55L, includeArchived = true)

        assertThat(hiddenResult).isEmpty()
        assertThat(visibleResult).hasSize(1)
        assertThat(visibleResult.first().clientArchived).isTrue()
    }

    @Test
    fun getExistingDtosByClientId_returnsStartEndAndSponsorName() {
        whenever(assistancePlanRepository.findExistingProjectionsByClientId(10L))
            .thenReturn(
                listOf(
                    existingProjection(
                        id = 9L,
                        start = LocalDate.of(2026, 1, 1),
                        end = LocalDate.of(2026, 3, 31),
                        sponsorName = "LWV"
                    )
                )
            )

        val result = previewService.getExistingDtosByClientId(10L)

        assertThat(result).hasSize(1)
        assertThat(result.first().id).isEqualTo(9L)
        assertThat(result.first().start).isEqualTo(LocalDate.of(2026, 1, 1))
        assertThat(result.first().end).isEqualTo(LocalDate.of(2026, 3, 31))
        assertThat(result.first().sponsorName).isEqualTo("LWV")
        assertThat(result.first().clientArchived).isFalse()
    }

    @ParameterizedTest
    @MethodSource("approvedAndExecutedMinuteCases")
    fun getPreviewDtosByClientId_withMinuteParts_formatsAsTimeDouble(
        weeklyPlanMinutes: Int,
        weeklyGoalMinutes: Int,
        executedMinutesA: Int,
        executedMinutesB: Int,
        expectedExecutedTimeDouble: Double
    ) {
        val now = LocalDate.now(clock)
        val yearStart = LocalDate.of(now.year, 1, 1)
        val periodEnd = now
        val planStart = now.minusDays(3)
        val planEnd = now.plusDays(3)
        val projection = previewProjection(55L, planStart, planEnd, false)

        whenever(assistancePlanRepository.findPreviewProjectionsByClientId(10L))
            .thenReturn(listOf(projection))
        whenever(assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(20L))
            .thenReturn(listOf(55L))
        whenever(assistancePlanRepository.findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(55L)))
            .thenReturn(
                if (weeklyPlanMinutes == 0) emptyList()
                else listOf(weeklyMinutesProjection(55L, weeklyPlanMinutes))
            )
        whenever(assistancePlanRepository.findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(55L)))
            .thenReturn(
                if (weeklyGoalMinutes == 0) emptyList()
                else listOf(weeklyMinutesProjection(55L, weeklyGoalMinutes))
            )
        whenever(serviceRepository.findMinutesByAssistancePlanIdsFromPlanStartToEnd(listOf(55L), periodEnd))
            .thenReturn(
                listOf(
                    serviceMinutesProjection(55L, executedMinutesA),
                    serviceMinutesProjection(55L, executedMinutesB)
                )
            )
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(55L), yearStart, periodEnd))
            .thenReturn(
                listOf(
                    serviceMinutesProjection(55L, executedMinutesA),
                    serviceMinutesProjection(55L, executedMinutesB)
                )
            )

        val result = previewService.getPreviewDtosByClientId(10L, 20L)

        assertThat(result).hasSize(1)
        val expectedApprovedWeeklyMinutes = if (weeklyPlanMinutes > 0) weeklyPlanMinutes else weeklyGoalMinutes
        assertThat(result.first().approvedHoursPerWeek).isEqualTo(
            TimeDoubleService.convertDoubleToTimeDouble(expectedApprovedWeeklyMinutes / 60.0)
        )
        assertThat(result.first().hasIllegalHours).isEqualTo(weeklyPlanMinutes > 0 && weeklyGoalMinutes > 0)
        assertThat(result.first().approvedHoursThisYear).isEqualTo(
            expectedApprovedHoursThisYear(
                expectedApprovedWeeklyMinutes.toDouble(),
                planStart,
                planEnd,
                yearStart,
                now
            )
        )
        assertThat(result.first().executedHoursThisYear).isEqualTo(expectedExecutedTimeDouble)
    }

    @Test
    fun getPreviewDtosByClientId_withCorridorPlan_usesCorridorRangeAndMode() {
        val now = LocalDate.now(clock)
        val yearStart = LocalDate.of(now.year, 1, 1)
        val periodEnd = now
        val planStart = now.minusDays(3)
        val planEnd = now.plusDays(3)
        val projection = previewProjection(
            planId = 77L,
            planStart = planStart,
            planEnd = planEnd,
            clientArchived = false,
            hourMode = AssistancePlanHourMode.CORRIDOR,
            hourCorridorWeeklyMinutesFrom = 300,
            hourCorridorWeeklyMinutesTill = 600
        )

        whenever(assistancePlanRepository.findPreviewProjectionsByClientId(10L))
            .thenReturn(listOf(projection))
        whenever(assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(20L))
            .thenReturn(listOf(77L))
        whenever(assistancePlanRepository.findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(77L)))
            .thenReturn(emptyList())
        whenever(assistancePlanRepository.findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(77L)))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsFromPlanStartToEnd(listOf(77L), periodEnd))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(77L), yearStart, periodEnd))
            .thenReturn(emptyList())

        val result = previewService.getPreviewDtosByClientId(10L, 20L)

        assertThat(result).hasSize(1)
        assertThat(result.first().hourMode).isEqualTo(AssistancePlanHourMode.CORRIDOR)
        assertThat(result.first().approvedHoursFrom).isEqualTo(5.0)
        assertThat(result.first().approvedHoursTo).isEqualTo(10.0)
        assertThat(result.first().approvedHoursPerWeek).isEqualTo(7.3)
        assertThat(result.first().approvedHoursThisYearFrom)
            .isEqualTo(expectedApprovedHoursThisYear(300.0, planStart, planEnd, yearStart, now))
        assertThat(result.first().approvedHoursThisYearTill)
            .isEqualTo(expectedApprovedHoursThisYear(600.0, planStart, planEnd, yearStart, now))
        assertThat(result.first().approvedHoursThisYear)
            .isEqualTo(expectedApprovedHoursThisYear(450.0, planStart, planEnd, yearStart, now))
        assertThat(result.first().approvedHoursLeftThisYear)
            .isEqualTo(result.first().approvedHoursThisYearFrom)
        assertThat(result.first().hasIllegalHours).isFalse()
    }

    @Test
    fun getPreviewDtosByClientId_withCorridorPlan_returnsZeroWhenExecutedHoursAreWithinRange() {
        val now = LocalDate.now(clock)
        val yearStart = LocalDate.of(now.year, 1, 1)
        val projection = previewProjection(
            planId = 78L,
            planStart = yearStart,
            planEnd = LocalDate.of(now.year, 12, 31),
            clientArchived = false,
            hourMode = AssistancePlanHourMode.CORRIDOR,
            hourCorridorWeeklyMinutesFrom = 60,
            hourCorridorWeeklyMinutesTill = 120
        )

        whenever(assistancePlanRepository.findPreviewProjectionsByClientId(10L)).thenReturn(listOf(projection))
        whenever(assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(20L)).thenReturn(emptyList())
        whenever(assistancePlanRepository.findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(78L)))
            .thenReturn(emptyList())
        whenever(assistancePlanRepository.findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(78L)))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsFromPlanStartToEnd(listOf(78L), now))
            .thenReturn(listOf(serviceMinutesProjection(78L, 2_400)))
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(78L), yearStart, now))
            .thenReturn(listOf(serviceMinutesProjection(78L, 2_400)))

        val result = previewService.getPreviewDtosByClientId(10L, 20L).first()

        assertThat(result.executedHoursThisYear).isBetween(
            result.approvedHoursThisYearFrom,
            result.approvedHoursThisYearTill
        )
        assertThat(result.approvedHoursLeftThisYear).isEqualTo(0.0)
    }

    @Test
    fun getPreviewDtosByClientId_withCorridorPlan_returnsNegativeDifferenceAboveUpperBound() {
        val now = LocalDate.now(clock)
        val yearStart = LocalDate.of(now.year, 1, 1)
        val projection = previewProjection(
            planId = 79L,
            planStart = yearStart,
            planEnd = LocalDate.of(now.year, 12, 31),
            clientArchived = false,
            hourMode = AssistancePlanHourMode.CORRIDOR,
            hourCorridorWeeklyMinutesFrom = 60,
            hourCorridorWeeklyMinutesTill = 120
        )

        whenever(assistancePlanRepository.findPreviewProjectionsByClientId(10L)).thenReturn(listOf(projection))
        whenever(assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(20L)).thenReturn(emptyList())
        whenever(assistancePlanRepository.findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(79L)))
            .thenReturn(emptyList())
        whenever(assistancePlanRepository.findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(79L)))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsFromPlanStartToEnd(listOf(79L), now))
            .thenReturn(listOf(serviceMinutesProjection(79L, 4_000)))
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(79L), yearStart, now))
            .thenReturn(listOf(serviceMinutesProjection(79L, 4_000)))

        val result = previewService.getPreviewDtosByClientId(10L, 20L).first()

        assertThat(result.executedHoursThisYear).isGreaterThan(result.approvedHoursThisYearTill)
        assertThat(result.approvedHoursLeftThisYear).isEqualTo(
            TimeDoubleService.diffTimeDoubles(result.approvedHoursThisYearTill, result.executedHoursThisYear)
        )
    }

    private fun expectedApprovedHoursThisYear(
        approvedWeeklyMinutes: Double,
        planStart: LocalDate,
        planEnd: LocalDate,
        yearStart: LocalDate,
        yearEnd: LocalDate
    ): Double {
        val overlapStart = if (planStart.isAfter(yearStart)) planStart else yearStart
        val overlapEnd = if (planEnd.isBefore(yearEnd)) planEnd else yearEnd
        if (overlapEnd.isBefore(overlapStart)) {
            return 0.0
        }

        val days = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1
        val hours = (days * (approvedWeeklyMinutes / 7.0)) / 60.0
        return TimeDoubleService.convertDoubleToTimeDouble(hours)
    }

    private fun previewProjection(
        planId: Long,
        planStart: LocalDate,
        planEnd: LocalDate,
        clientArchived: Boolean,
        hourMode: AssistancePlanHourMode = AssistancePlanHourMode.EXACT,
        hourCorridorWeeklyMinutesFrom: Int? = null,
        hourCorridorWeeklyMinutesTill: Int? = null
    ): AssistancePlanPreviewProjection {
        return object : AssistancePlanPreviewProjection {
            override val id: Long = planId
            override val clientId: Long = 101
            override val institutionId: Long = 102
            override val sponsorId: Long = 103
            override val clientFirstname: String = "Max"
            override val clientLastname: String = "Mustermann"
            override val institutionName: String = "Schule"
            override val sponsorName: String = "Kostentraeger"
            override val clientArchived: Boolean = clientArchived
            override val hourMode: AssistancePlanHourMode = hourMode
            override val hourCorridorWeeklyMinutesFrom: Int? = hourCorridorWeeklyMinutesFrom
            override val hourCorridorWeeklyMinutesTill: Int? = hourCorridorWeeklyMinutesTill
            override val start: LocalDate = planStart
            override val end: LocalDate = planEnd
        }
    }

    private fun weeklyMinutesProjection(assistancePlanId: Long, weeklyMinutes: Int): AssistancePlanWeeklyMinutesProjection {
        return object : AssistancePlanWeeklyMinutesProjection {
            override val assistancePlanId: Long = assistancePlanId
            override val weeklyMinutes: Int = weeklyMinutes
        }
    }

    private fun serviceMinutesProjection(assistancePlanId: Long, minutes: Int): AssistancePlanServiceMinutesProjection {
        return object : AssistancePlanServiceMinutesProjection {
            override val assistancePlanId: Long = assistancePlanId
            override val minutes: Int = minutes
        }
    }

    private fun existingProjection(
        id: Long,
        start: LocalDate,
        end: LocalDate,
        sponsorName: String
    ): AssistancePlanExistingProjection {
        return object : AssistancePlanExistingProjection {
            override val id: Long = id
            override val start: LocalDate = start
            override val end: LocalDate = end
            override val sponsorName: String = sponsorName
            override val clientArchived: Boolean = false
        }
    }

    companion object {
        @JvmStatic
        fun approvedAndExecutedMinuteCases(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(95, 40, 65, 60, 2.05),
                Arguments.of(61, 0, 29, 30, 0.59),
                Arguments.of(120, 35, 31, 31, 1.02)
            )
        }
    }
}
