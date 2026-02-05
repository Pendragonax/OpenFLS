package de.vinz.openfls.domains.services.services

import de.vinz.openfls.domains.absence.AbsenceService
import de.vinz.openfls.domains.absence.dtos.EmployeeAbsenceResponseDTO
import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.contingents.services.ContingentCalendarService
import de.vinz.openfls.domains.contingents.services.ContingentService
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.domains.services.projections.ServiceCalendarProjection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

class ContingentCalendarServiceTest {
    private val serviceRepository: ServiceRepository = mock()
    private val contingentService: ContingentService = mock()
    private val absenceService: AbsenceService = mock()
    private val contingentCalendarService = ContingentCalendarService(serviceRepository, contingentService, absenceService)

    @Test
    fun generateContingentCalendarInformationFor_multipleServicesSameDay_aggregatesMinutesAndContingent() {
        // Given
        val employeeId = 7L
        val now = LocalDate.now()
        val start = now.minusYears(1)
        val serviceDate = now.minusDays(2)
        val otherDate = now.minusDays(1)
        val contingent = ContingentDto().apply {
            this.start = now.minusMonths(1)
            this.end = null
            this.weeklyServiceHours = 10.0
        }
        val projections = listOf(
            TestServiceCalendarProjection(
                id = 1,
                start = serviceDate.atTime(9, 0),
                minutes = 60
            ),
            TestServiceCalendarProjection(
                id = 2,
                start = serviceDate.atTime(13, 0),
                minutes = 30
            ),
            TestServiceCalendarProjection(
                id = 3,
                start = otherDate.atTime(10, 0),
                minutes = 240
            )
        )

        whenever(contingentService.getByEmployeeId(employeeId)).thenReturn(listOf(contingent))
        whenever(serviceRepository.findServiceCalendarProjection(employeeId, start, now)).thenReturn(projections)
        whenever(absenceService.getAllByEmployeeId(employeeId)).thenReturn(
            EmployeeAbsenceResponseDTO(employeeId, emptyList())
        )
        whenever(contingentService.calculateContingentMinutesForWorkdayBy(serviceDate, listOf(contingent)))
            .thenReturn(120.0)
        whenever(contingentService.calculateContingentMinutesForWorkdayBy(otherDate, listOf(contingent)))
            .thenReturn(120.0)
        whenever(contingentService.calculateContingentMinutesForWorkdayBy(now, listOf(contingent)))
            .thenReturn(60.0)
        whenever(contingentService.calculateContingentMinutesFor(any(), any(), eq(listOf(contingent))))
            .thenReturn(300)

        // When
        val result = contingentCalendarService.generateContingentCalendarInformationFor(employeeId, now)

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
    fun generateContingentCalendarInformationFor_absenceToday_createsAbsentDayAndZeroTodayTotals() {
        // Given
        val employeeId = 11L
        val now = LocalDate.now()
        val start = now.minusYears(1)
        val contingent = ContingentDto().apply {
            this.start = now.minusMonths(2)
            this.end = null
            this.weeklyServiceHours = 20.0
        }
        val absences = listOf(now)

        whenever(contingentService.getByEmployeeId(employeeId)).thenReturn(listOf(contingent))
        whenever(serviceRepository.findServiceCalendarProjection(employeeId, start, now)).thenReturn(emptyList())
        whenever(absenceService.getAllByEmployeeId(employeeId)).thenReturn(
            EmployeeAbsenceResponseDTO(employeeId, absences)
        )
        whenever(contingentService.calculateContingentMinutesForWorkdayBy(now, listOf(contingent)))
            .thenReturn(120.0)
        whenever(contingentService.calculateContingentMinutesFor(any(), any(), eq(listOf(contingent))))
            .thenReturn(300)

        // When
        val result = contingentCalendarService.generateContingentCalendarInformationFor(employeeId, now)

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
    fun generateContingentCalendarInformationFor_absenceAndServiceSameDay_marksAbsentWithoutExtraDay() {
        // Given
        val employeeId = 3L
        val now = LocalDate.now()
        val start = now.minusYears(1)
        val serviceDate = now.minusDays(1)
        val contingent = ContingentDto().apply {
            this.start = now.minusMonths(3)
            this.end = null
            this.weeklyServiceHours = 35.0
        }
        val projections = listOf(
            TestServiceCalendarProjection(
                id = 9,
                start = serviceDate.atTime(9, 0),
                minutes = 60
            )
        )
        val absences = listOf(serviceDate)

        whenever(contingentService.getByEmployeeId(employeeId)).thenReturn(listOf(contingent))
        whenever(serviceRepository.findServiceCalendarProjection(employeeId, start, now)).thenReturn(projections)
        whenever(absenceService.getAllByEmployeeId(employeeId)).thenReturn(
            EmployeeAbsenceResponseDTO(employeeId, absences)
        )
        whenever(contingentService.calculateContingentMinutesForWorkdayBy(serviceDate, listOf(contingent)))
            .thenReturn(120.0)
        whenever(contingentService.calculateContingentMinutesForWorkdayBy(now, listOf(contingent)))
            .thenReturn(120.0)

        // When
        val result = contingentCalendarService.generateContingentCalendarInformationFor(employeeId, now)

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
    fun generateContingentCalendarInformationFor_noContingent_returnsZeroContingentTotals() {
        // Given
        val employeeId = 15L
        val now = LocalDate.now()
        val start = now.minusYears(1)
        val projections = listOf(
            TestServiceCalendarProjection(
                id = 12,
                start = now.atTime(8, 0),
                minutes = 61
            )
        )

        whenever(contingentService.getByEmployeeId(employeeId)).thenReturn(emptyList())
        whenever(serviceRepository.findServiceCalendarProjection(employeeId, start, now)).thenReturn(projections)
        whenever(absenceService.getAllByEmployeeId(employeeId)).thenReturn(
            EmployeeAbsenceResponseDTO(employeeId, emptyList())
        )
        whenever(contingentService.calculateContingentMinutesForWorkdayBy(now, emptyList()))
            .thenReturn(0.0)
        whenever(contingentService.calculateContingentMinutesFor(any(), any(), eq(emptyList())))
            .thenReturn(0)

        // When
        val result = contingentCalendarService.generateContingentCalendarInformationFor(employeeId, now)

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

    private data class TestServiceCalendarProjection(
        override val id: Long,
        override val start: LocalDateTime,
        override val minutes: Int
    ) : ServiceCalendarProjection
}
