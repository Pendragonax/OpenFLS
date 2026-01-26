package de.vinz.openfls.domains.services.services

import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.contingents.services.ContingentService
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.domains.services.projections.ServiceCalendarProjection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

class CalendarServiceTest {
    private val serviceRepository: ServiceRepository = mock()
    private val contingentService: ContingentService = mock()
    private val calendarService = CalendarService(serviceRepository, contingentService)

    @Test
    fun getServiceCalendarInformation_multipleServicesSameDay_aggregatesMinutesAndContingent() {
        // Given
        val employeeId = 7L
        val end = LocalDate.now()
        val start = end.minusYears(1)
        val contingent = ContingentDto().apply {
            this.start = end.minusMonths(2)
            this.end = null
            this.weeklyServiceHours = 10.0
        }
        val projections = listOf(
            TestServiceCalendarProjection(
                id = 1,
                start = end.minusDays(2).atTime(9, 0),
                minutes = 60
            ),
            TestServiceCalendarProjection(
                id = 2,
                start = end.minusDays(2).atTime(13, 0),
                minutes = 30
            ),
            TestServiceCalendarProjection(
                id = 3,
                start = end.minusDays(1).atTime(10, 0),
                minutes = 240
            )
        )
        whenever(contingentService.getByEmployeeId(employeeId)).thenReturn(listOf(contingent))
        whenever(serviceRepository.findServiceCalendarProjection(employeeId, start, end)).thenReturn(projections)

        // When
        val result = calendarService.getServiceCalendarInformation(employeeId, end)

        // Then
        assertThat(result.employeeId).isEqualTo(employeeId)
        assertThat(result.days).hasSize(2)
        assertThat(result.days).allSatisfy { day ->
            assertThat(day.date).isEqualTo(LocalDate.now())
            assertThat(day.serviceCount).isEqualTo(0)
        }
        assertThat(result.days).anySatisfy { day ->
            assertThat(day.executedHours).isEqualTo(1)
            assertThat(day.executedMinutes).isEqualTo(30)
            assertThat(day.contingentHours).isEqualTo(2)
            assertThat(day.contingentMinutes).isEqualTo(0)
            assertThat(day.differenceHours).isEqualTo(0)
            assertThat(day.differenceMinutes).isEqualTo(-30)
        }
        assertThat(result.days).anySatisfy { day ->
            assertThat(day.executedHours).isEqualTo(4)
            assertThat(day.executedMinutes).isEqualTo(0)
            assertThat(day.contingentHours).isEqualTo(2)
            assertThat(day.contingentMinutes).isEqualTo(0)
            assertThat(day.differenceHours).isEqualTo(2)
            assertThat(day.differenceMinutes).isEqualTo(0)
        }
        verify(serviceRepository).findServiceCalendarProjection(employeeId, start, end)
        verify(contingentService).getByEmployeeId(employeeId)
    }

    @Test
    fun getServiceCalendarInformation_noContingent_returnsZeroContingentValues() {
        // Given
        val employeeId = 11L
        val end = LocalDate.now()
        val start = end.minusYears(1)
        val projection = TestServiceCalendarProjection(
            id = 5,
            start = end.atTime(8, 0),
            minutes = 61
        )
        whenever(contingentService.getByEmployeeId(employeeId)).thenReturn(emptyList())
        whenever(serviceRepository.findServiceCalendarProjection(employeeId, start, end)).thenReturn(listOf(projection))

        // When
        val result = calendarService.getServiceCalendarInformation(employeeId, end)

        // Then
        assertThat(result.days).hasSize(1)
        assertThat(result.days.first().date).isEqualTo(LocalDate.now())
        assertThat(result.days.first().serviceCount).isEqualTo(0)
        assertThat(result.days.first().executedHours).isEqualTo(1)
        assertThat(result.days.first().executedMinutes).isEqualTo(1)
        assertThat(result.days.first().contingentHours).isEqualTo(0)
        assertThat(result.days.first().contingentMinutes).isEqualTo(0)
        assertThat(result.days.first().differenceHours).isEqualTo(1)
        assertThat(result.days.first().differenceMinutes).isEqualTo(1)
        assertThat(result.today.executedHours).isEqualTo(1)
        assertThat(result.today.executedMinutes).isEqualTo(1)
        assertThat(result.today.contingentHours).isEqualTo(0)
        assertThat(result.today.contingentMinutes).isEqualTo(0)
        assertThat(result.lastWeek.executedHours).isEqualTo(1)
        assertThat(result.lastWeek.executedMinutes).isEqualTo(1)
        assertThat(result.lastWeek.contingentHours).isEqualTo(0)
        assertThat(result.lastWeek.contingentMinutes).isEqualTo(0)
        assertThat(result.lastMonth.executedHours).isEqualTo(1)
        assertThat(result.lastMonth.executedMinutes).isEqualTo(1)
        assertThat(result.lastMonth.contingentHours).isEqualTo(0)
        assertThat(result.lastMonth.contingentMinutes).isEqualTo(0)
        verify(serviceRepository).findServiceCalendarProjection(employeeId, start, end)
        verify(contingentService).getByEmployeeId(employeeId)
    }

    @Test
    fun getServiceCalendarInformation_activeContingent_calculatesTodayLastWeekLastMonth() {
        // Given
        val employeeId = 3L
        val end = LocalDate.now()
        val start = end.minusYears(1)
        val contingent = ContingentDto().apply {
            this.start = end.minusMonths(3)
            this.end = null
            this.weeklyServiceHours = 35.0
        }
        val projections = listOf(
            TestServiceCalendarProjection(
                id = 9,
                start = end.minusDays(5).atTime(9, 0),
                minutes = 120
            ),
            TestServiceCalendarProjection(
                id = 10,
                start = end.minusDays(5).atTime(13, 0),
                minutes = 60
            ),
            TestServiceCalendarProjection(
                id = 11,
                start = end.minusDays(10).atTime(9, 0),
                minutes = 240
            )
        )
        whenever(contingentService.getByEmployeeId(employeeId)).thenReturn(listOf(contingent))
        whenever(serviceRepository.findServiceCalendarProjection(employeeId, start, end)).thenReturn(projections)

        // When
        val result = calendarService.getServiceCalendarInformation(employeeId, end)

        // Then
        val expectedTodayExecutedMinutes = 180
        val expectedTodayContingentMinutes = 420
        val expectedTodayDifferenceMinutes = expectedTodayExecutedMinutes - expectedTodayContingentMinutes
        assertThat(result.today.executedHours).isEqualTo(expectedTodayExecutedMinutes / 60)
        assertThat(result.today.executedMinutes).isEqualTo(expectedTodayExecutedMinutes % 60)
        assertThat(result.today.contingentHours).isEqualTo(expectedTodayContingentMinutes / 60)
        assertThat(result.today.contingentMinutes).isEqualTo(expectedTodayContingentMinutes % 60)
        assertThat(result.today.differenceHours).isEqualTo(expectedTodayDifferenceMinutes / 60)
        assertThat(result.today.differenceMinutes).isEqualTo(expectedTodayDifferenceMinutes % 60)

        val expectedRangeExecutedMinutes = 420
        val contingentMinutesPerDay = 300
        val lastWeekDays = 7
        val expectedLastWeekContingentMinutes = lastWeekDays * contingentMinutesPerDay
        val expectedLastWeekDifferenceMinutes = expectedRangeExecutedMinutes - expectedLastWeekContingentMinutes
        assertThat(result.lastWeek.executedHours).isEqualTo(expectedRangeExecutedMinutes / 60)
        assertThat(result.lastWeek.executedMinutes).isEqualTo(expectedRangeExecutedMinutes % 60)
        assertThat(result.lastWeek.contingentHours).isEqualTo(expectedLastWeekContingentMinutes / 60)
        assertThat(result.lastWeek.contingentMinutes).isEqualTo(expectedLastWeekContingentMinutes % 60)
        assertThat(result.lastWeek.differenceHours).isEqualTo(expectedLastWeekDifferenceMinutes / 60)
        assertThat(result.lastWeek.differenceMinutes).isEqualTo(expectedLastWeekDifferenceMinutes % 60)

        val lastMonthDays = java.time.temporal.ChronoUnit.DAYS.between(end.minusMonths(1), end) + 1
        val expectedLastMonthContingentMinutes = lastMonthDays.toInt() * contingentMinutesPerDay
        val expectedLastMonthDifferenceMinutes = expectedRangeExecutedMinutes - expectedLastMonthContingentMinutes
        assertThat(result.lastMonth.executedHours).isEqualTo(expectedRangeExecutedMinutes / 60)
        assertThat(result.lastMonth.executedMinutes).isEqualTo(expectedRangeExecutedMinutes % 60)
        assertThat(result.lastMonth.contingentHours).isEqualTo(expectedLastMonthContingentMinutes / 60)
        assertThat(result.lastMonth.contingentMinutes).isEqualTo(expectedLastMonthContingentMinutes % 60)
        assertThat(result.lastMonth.differenceHours).isEqualTo(expectedLastMonthDifferenceMinutes / 60)
        assertThat(result.lastMonth.differenceMinutes).isEqualTo(expectedLastMonthDifferenceMinutes % 60)
        verify(serviceRepository).findServiceCalendarProjection(employeeId, start, end)
        verify(contingentService).getByEmployeeId(employeeId)
    }

    private data class TestServiceCalendarProjection(
        override val id: Long,
        override val start: LocalDateTime,
        override val minutes: Int
    ) : ServiceCalendarProjection
}
