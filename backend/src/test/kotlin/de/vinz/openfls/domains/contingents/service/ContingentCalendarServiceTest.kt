package de.vinz.openfls.domains.contingents.service

import de.vinz.openfls.domains.absence.service.AbsenceService
import de.vinz.openfls.domains.absence.entity.Absence
import de.vinz.openfls.domains.contingents.entity.Contingent
import de.vinz.openfls.domains.services.service.ServiceService
import de.vinz.openfls.domains.services.dto.ServiceCalendarDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ContingentCalendarServiceTest {
    private val today: LocalDate = LocalDate.of(2026, 3, 11)
    private val clock: Clock = Clock.fixed(Instant.parse("2026-03-11T09:00:00Z"), ZoneId.of("UTC"))
    private val serviceService: ServiceService = mock()
    private val contingentService: ContingentService = mock()
    private val contingentCalculationService: ContingentCalculationService = mock()
    private val absenceService: AbsenceService = mock()
    private val contingentCalendarService = ContingentCalendarService(serviceService, contingentService, contingentCalculationService, absenceService, clock)

    @Test
    fun generateContingentCalendarFor_multipleServicesSameDay_aggregatesMinutesAndContingent() {
        // Given
        val employeeId = 7L
        val now = today
        val start = now.minusYears(1)
        val serviceDate = now.minusDays(2)
        val otherDate = now.minusDays(1)
        val contingent = Contingent().apply {
            this.start = now.minusMonths(1)
            this.end = null
            this.weeklyServiceHours = 10.0
        }
        val projections = listOf(
            ServiceCalendarDto(
                id = 1,
                start = serviceDate.atTime(9, 0),
                minutes = 60
            ),
            ServiceCalendarDto(
                id = 2,
                start = serviceDate.atTime(13, 0),
                minutes = 30
            ),
            ServiceCalendarDto(
                id = 3,
                start = otherDate.atTime(10, 0),
                minutes = 240
            )
        )

        whenever(contingentService.getAllEntitiesByEmployeeId(employeeId)).thenReturn(listOf(contingent))
        whenever(serviceService.getCalendarServicesByEmployeeIdAndStartAndEnd(employeeId, start, now)).thenReturn(projections)
        whenever(absenceService.getAllEntitiesByEmployeeId(employeeId)).thenReturn(emptyList())
        whenever(contingentCalculationService.calculateContingentMinutesForWorkdayBy(serviceDate, listOf(contingent)))
            .thenReturn(120.0)
        whenever(contingentCalculationService.calculateContingentMinutesForWorkdayBy(otherDate, listOf(contingent)))
            .thenReturn(120.0)
        whenever(contingentCalculationService.calculateContingentMinutesForWorkdayBy(now, listOf(contingent)))
            .thenReturn(60.0)
        whenever(contingentCalculationService.calculateContingentMinutesFor(any(), any(), eq(listOf(contingent))))
            .thenReturn(300)

        // When
        val result = contingentCalendarService.generateContingentCalendarFor(employeeId, now)

        // Then
        assertThat(result.employeeId).isEqualTo(employeeId)
        assertThat(result.days).hasSize(2)

        val aggregatedDay = result.days.first { it.date == serviceDate }
        assertThat(aggregatedDay.executedHours).isEqualTo(1)
        assertThat(aggregatedDay.executedMinutes).isEqualTo(30)
        assertThat(aggregatedDay.contingentHours).isEqualTo(2)
        assertThat(aggregatedDay.contingentMinutes).isEqualTo(0)
        assertThat(aggregatedDay.differenceHours).isEqualTo(0)
        assertThat(aggregatedDay.differenceMinutes).isEqualTo(-30)
        assertThat(aggregatedDay.absence).isFalse()

        val otherDay = result.days.first { it.date == otherDate }
        assertThat(otherDay.executedHours).isEqualTo(4)
        assertThat(otherDay.executedMinutes).isEqualTo(0)
        assertThat(otherDay.contingentHours).isEqualTo(2)
        assertThat(otherDay.contingentMinutes).isEqualTo(0)
        assertThat(otherDay.differenceHours).isEqualTo(2)
        assertThat(otherDay.differenceMinutes).isEqualTo(0)
        assertThat(otherDay.absence).isFalse()

        assertThat(result.today.executedHours).isEqualTo(0)
        assertThat(result.today.executedMinutes).isEqualTo(0)
        assertThat(result.today.contingentHours).isEqualTo(1)
        assertThat(result.today.contingentMinutes).isEqualTo(0)
        assertThat(result.today.differenceHours).isEqualTo(-1)
        assertThat(result.today.differenceMinutes).isEqualTo(0)
    }

    @Test
    fun generateContingentCalendarFor_absenceToday_createsAbsentDayAndZeroTodayTotals() {
        // Given
        val employeeId = 11L
        val now = today
        val start = now.minusYears(1)
        val contingent = Contingent().apply {
            this.start = now.minusMonths(2)
            this.end = null
            this.weeklyServiceHours = 20.0
        }
        val absences = listOf(now)

        whenever(contingentService.getAllEntitiesByEmployeeId(employeeId)).thenReturn(listOf(contingent))
        whenever(serviceService.getCalendarServicesByEmployeeIdAndStartAndEnd(employeeId, start, now)).thenReturn(emptyList())
        whenever(absenceService.getAllEntitiesByEmployeeId(employeeId)).thenReturn(
            absences.map { Absence(absenceDate = it, employeeId = employeeId) }
        )
        whenever(contingentCalculationService.calculateContingentMinutesForWorkdayBy(now, listOf(contingent)))
            .thenReturn(120.0)
        whenever(contingentCalculationService.calculateContingentMinutesFor(any(), any(), eq(listOf(contingent))))
            .thenReturn(300)

        // When
        val result = contingentCalendarService.generateContingentCalendarFor(employeeId, now)

        // Then
        assertThat(result.days).hasSize(1)
        val absentDay = result.days.first()
        assertThat(absentDay.date).isEqualTo(now)
        assertThat(absentDay.absence).isTrue()
        assertThat(absentDay.executedHours).isEqualTo(0)
        assertThat(absentDay.executedMinutes).isEqualTo(0)
        assertThat(absentDay.contingentHours).isEqualTo(0)
        assertThat(absentDay.contingentMinutes).isEqualTo(0)

        assertThat(result.today.executedHours).isEqualTo(0)
        assertThat(result.today.executedMinutes).isEqualTo(0)
        assertThat(result.today.contingentHours).isEqualTo(0)
        assertThat(result.today.contingentMinutes).isEqualTo(0)
        assertThat(result.today.differenceHours).isEqualTo(0)
        assertThat(result.today.differenceMinutes).isEqualTo(0)

        assertThat(result.thisWeek.contingentHours).isEqualTo(3)
        assertThat(result.thisWeek.contingentMinutes).isEqualTo(0)
        assertThat(result.thisWeek.differenceHours).isEqualTo(-3)
        assertThat(result.thisWeek.differenceMinutes).isEqualTo(0)
    }

    @Test
    fun generateContingentCalendarFor_absenceAndServiceSameDay_marksAbsentWithoutExtraDay() {
        // Given
        val employeeId = 3L
        val now = today
        val start = now.minusYears(1)
        val serviceDate = now.minusDays(1)
        val contingent = Contingent().apply {
            this.start = now.minusMonths(3)
            this.end = null
            this.weeklyServiceHours = 35.0
        }
        val projections = listOf(
            ServiceCalendarDto(
                id = 9,
                start = serviceDate.atTime(9, 0),
                minutes = 60
            )
        )
        val absences = listOf(serviceDate)

        whenever(contingentService.getAllEntitiesByEmployeeId(employeeId)).thenReturn(listOf(contingent))
        whenever(serviceService.getCalendarServicesByEmployeeIdAndStartAndEnd(employeeId, start, now)).thenReturn(projections)
        whenever(absenceService.getAllEntitiesByEmployeeId(employeeId)).thenReturn(
            absences.map { Absence(absenceDate = it, employeeId = employeeId) }
        )
        whenever(contingentCalculationService.calculateContingentMinutesForWorkdayBy(serviceDate, listOf(contingent)))
            .thenReturn(120.0)
        whenever(contingentCalculationService.calculateContingentMinutesForWorkdayBy(now, listOf(contingent)))
            .thenReturn(120.0)

        // When
        val result = contingentCalendarService.generateContingentCalendarFor(employeeId, now)

        // Then
        assertThat(result.days).hasSize(1)
        val dayInformation = result.days.first()
        assertThat(dayInformation.date).isEqualTo(serviceDate)
        assertThat(dayInformation.absence).isTrue()
        assertThat(dayInformation.executedHours).isEqualTo(1)
        assertThat(dayInformation.executedMinutes).isEqualTo(0)
        assertThat(dayInformation.contingentHours).isEqualTo(2)
        assertThat(dayInformation.contingentMinutes).isEqualTo(0)
        assertThat(dayInformation.differenceHours).isEqualTo(-1)
        assertThat(dayInformation.differenceMinutes).isEqualTo(0)
    }

    @Test
    fun generateContingentCalendarFor_noContingent_returnsZeroContingentTotals() {
        // Given
        val employeeId = 15L
        val now = today
        val start = now.minusYears(1)
        val projections = listOf(
            ServiceCalendarDto(
                id = 12,
                start = now.atTime(8, 0),
                minutes = 61
            )
        )

        whenever(contingentService.getAllEntitiesByEmployeeId(employeeId)).thenReturn(emptyList())
        whenever(serviceService.getCalendarServicesByEmployeeIdAndStartAndEnd(employeeId, start, now)).thenReturn(projections)
        whenever(absenceService.getAllEntitiesByEmployeeId(employeeId)).thenReturn(emptyList())
        whenever(contingentCalculationService.calculateContingentMinutesForWorkdayBy(now, emptyList()))
            .thenReturn(0.0)
        whenever(contingentCalculationService.calculateContingentMinutesFor(any(), any(), eq(emptyList())))
            .thenReturn(0)

        // When
        val result = contingentCalendarService.generateContingentCalendarFor(employeeId, now)

        // Then
        assertThat(result.days).hasSize(1)
        val dayInformation = result.days.first()
        assertThat(dayInformation.date).isEqualTo(now)
        assertThat(dayInformation.executedHours).isEqualTo(1)
        assertThat(dayInformation.executedMinutes).isEqualTo(1)
        assertThat(dayInformation.contingentHours).isEqualTo(0)
        assertThat(dayInformation.contingentMinutes).isEqualTo(0)
        assertThat(dayInformation.differenceHours).isEqualTo(1)
        assertThat(dayInformation.differenceMinutes).isEqualTo(1)

        assertThat(result.today.executedHours).isEqualTo(1)
        assertThat(result.today.executedMinutes).isEqualTo(1)
        assertThat(result.today.contingentHours).isEqualTo(0)
        assertThat(result.today.contingentMinutes).isEqualTo(0)
        assertThat(result.today.differenceHours).isEqualTo(1)
        assertThat(result.today.differenceMinutes).isEqualTo(1)
    }
}
