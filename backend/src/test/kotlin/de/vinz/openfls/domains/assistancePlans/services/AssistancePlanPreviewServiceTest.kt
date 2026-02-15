package de.vinz.openfls.domains.assistancePlans.services

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
import java.time.LocalDate
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

    private lateinit var previewService: AssistancePlanPreviewService

    @BeforeEach
    fun setUp() {
        previewService = AssistancePlanPreviewService(assistancePlanRepository, serviceRepository)
    }

    @Test
    fun getPreviewDtosByClientId_calculatesApprovedAndExecutedHoursInKotlin() {
        val now = LocalDate.now()
        val yearStart = LocalDate.of(now.year, 1, 1)
        val periodEnd = now

        val projection = previewProjection(5L, yearStart, now.plusDays(30))

        whenever(assistancePlanRepository.findPreviewProjectionsByClientId(10L))
            .thenReturn(listOf(projection))
        whenever(assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(20L))
            .thenReturn(listOf(5L))
        whenever(assistancePlanRepository.findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(5L)))
            .thenReturn(listOf(weeklyMinutesProjection(5L, 120)))
        whenever(assistancePlanRepository.findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(5L)))
            .thenReturn(listOf(weeklyMinutesProjection(5L, 300)))
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(5L), yearStart, periodEnd))
            .thenReturn(
                listOf(
                    serviceMinutesProjection(5L, 120),
                    serviceMinutesProjection(5L, 60)
                )
            )

        val result = previewService.getPreviewDtosByClientId(10L, 20L)

        assertThat(result).hasSize(1)
        assertThat(result.first().id).isEqualTo(5L)
        assertThat(result.first().isFavorite).isTrue()
        assertThat(result.first().approvedHoursPerWeek).isEqualTo(7.0)
        assertThat(result.first().executedHoursThisYear).isEqualTo(3.0)
        assertThat(result.first().approvedHoursThisYear)
            .isEqualTo(expectedApprovedHoursThisYear(420.0, yearStart, now.plusDays(30), yearStart, now))
        verify(serviceRepository).findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(5L), yearStart, now)
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
        verify(serviceRepository, never()).findMinutesByAssistancePlanIdsAndStartAndEnd(any(), any(), any())
    }

    @Test
    fun getFavoritePreviewDtosByEmployeeId_marksAllReturnedPlansAsFavorite() {
        val now = LocalDate.now()
        val yearStart = LocalDate.of(now.year, 1, 1)
        val periodEnd = now

        val projection = previewProjection(7L, now.minusDays(10), now.plusDays(10))

        whenever(assistancePlanRepository.findFavoritePreviewProjectionsByEmployeeId(33L))
            .thenReturn(listOf(projection))
        whenever(assistancePlanRepository.findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(7L)))
            .thenReturn(listOf(weeklyMinutesProjection(7L, 210)))
        whenever(assistancePlanRepository.findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(7L)))
            .thenReturn(emptyList())
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(7L), yearStart, periodEnd))
            .thenReturn(emptyList())

        val result = previewService.getFavoritePreviewDtosByEmployeeId(33L)

        assertThat(result).hasSize(1)
        assertThat(result.first().isFavorite).isTrue()
        assertThat(result.first().isActive).isTrue()
        assertThat(result.first().approvedHoursPerWeek).isEqualTo(3.3)
        assertThat(result.first().executedHoursThisYear).isEqualTo(0.0)
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
    }

    @ParameterizedTest
    @MethodSource("approvedAndExecutedMinuteCases")
    fun getPreviewDtosByClientId_withMinuteParts_formatsAsTimeDouble(
        weeklyPlanMinutes: Int,
        weeklyGoalMinutes: Int,
        executedMinutesA: Int,
        executedMinutesB: Int,
        expectedWeeklyTimeDouble: Double,
        expectedExecutedTimeDouble: Double
    ) {
        val now = LocalDate.now()
        val yearStart = LocalDate.of(now.year, 1, 1)
        val periodEnd = now
        val planStart = now.minusDays(3)
        val planEnd = now.plusDays(3)
        val projection = previewProjection(55L, planStart, planEnd)

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
        whenever(serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(listOf(55L), yearStart, periodEnd))
            .thenReturn(
                listOf(
                    serviceMinutesProjection(55L, executedMinutesA),
                    serviceMinutesProjection(55L, executedMinutesB)
                )
            )

        val result = previewService.getPreviewDtosByClientId(10L, 20L)

        assertThat(result).hasSize(1)
        assertThat(result.first().approvedHoursPerWeek).isEqualTo(expectedWeeklyTimeDouble)
        assertThat(result.first().approvedHoursThisYear).isEqualTo(
            expectedApprovedHoursThisYear(
                (weeklyPlanMinutes + weeklyGoalMinutes).toDouble(),
                planStart,
                planEnd,
                yearStart,
                now
            )
        )
        assertThat(result.first().executedHoursThisYear).isEqualTo(expectedExecutedTimeDouble)
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

    private fun previewProjection(planId: Long, planStart: LocalDate, planEnd: LocalDate): AssistancePlanPreviewProjection {
        return object : AssistancePlanPreviewProjection {
            override val id: Long = planId
            override val clientId: Long = 101
            override val institutionId: Long = 102
            override val sponsorId: Long = 103
            override val clientFirstname: String = "Max"
            override val clientLastname: String = "Mustermann"
            override val institutionName: String = "Schule"
            override val sponsorName: String = "Kostentraeger"
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
        }
    }

    companion object {
        @JvmStatic
        fun approvedAndExecutedMinuteCases(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(95, 40, 65, 60, 2.15, 2.05),
                Arguments.of(61, 0, 29, 30, 1.01, 0.59),
                Arguments.of(120, 35, 31, 31, 2.35, 1.02)
            )
        }
    }
}
