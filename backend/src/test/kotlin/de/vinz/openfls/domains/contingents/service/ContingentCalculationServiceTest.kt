package de.vinz.openfls.domains.contingents.service

import de.vinz.openfls.domains.absence.entity.Absence
import de.vinz.openfls.domains.contingents.entity.Contingent
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ContingentCalculationServiceTest {

    private val contingentCalculationService = ContingentCalculationService()

    @Test
    fun calculateContingentHoursBy_yearWithAbsences_returnsExpectedHours() {
        // Given
        val year = 2024
        val contingent = contingentWith(
            start = LocalDate.of(year, 1, 1),
            end = LocalDate.of(year, 1, 10),
            weeklyHours = 10.0
        )
        val absences = listOf(Absence(absenceDate = LocalDate.of(year, 1, 3), employeeId = contingent.employee!!.id!!))
        val workdays = DateService.calculateWorkdaysInHesseBetween(contingent.start, contingent.end, year)
        val dailyHours = contingent.weeklyServiceHours / 5
        val expectedTotal = TimeDoubleService.convertDoubleToTimeDouble((workdays - 1) * dailyHours)

        // When
        val result = contingentCalculationService.calculateContingentHoursBy(year, contingent, absences)

        // Then
        assertThat(result[0]).isEqualTo(expectedTotal)
        assertThat(result[1]).isEqualTo(12.0)
    }

    @Test
    fun calculateContingentHoursBy_monthOutsideContingent_returnsZero() {
        // Given
        val year = 2024
        val contingent = contingentWith(start = LocalDate.of(year, 3, 1))
        val absences = emptyList<Absence>()

        // When
        val result = contingentCalculationService.calculateContingentHoursBy(year, 1, contingent, absences)

        // Then
        assertThat(result).isEqualTo(0.0)
    }

    @Test
    fun calculateContingentHoursBy_monthWithAbsences_reducesHours() {
        // Given
        val year = 2024
        val contingent = contingentWith(
            start = LocalDate.of(year, 1, 1),
            end = LocalDate.of(year, 1, 31),
            weeklyHours = 10.0
        )
        val absences = listOf(Absence(absenceDate = LocalDate.of(year, 1, 3), employeeId = contingent.employee!!.id!!))
        val workdays = DateService.countWorkDaysOfMonthAndYearBetweenStartAndEnd(
            year,
            1,
            contingent.start,
            contingent.end!!
        )
        val expected = TimeDoubleService.convertDoubleToTimeDouble((workdays - 1) * (contingent.weeklyServiceHours / 5))

        // When
        val result = contingentCalculationService.calculateContingentHoursBy(year, 1, contingent, absences)

        // Then
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun countAbsenceDaysBy_filtersByEmployeeMonthAndContingent() {
        // Given
        val year = 2024
        val contingent = contingentWith(
            start = LocalDate.of(year, 1, 1),
            end = LocalDate.of(year, 1, 31)
        )
        val absences = listOf(
            Absence(absenceDate = LocalDate.of(year, 1, 1), employeeId = contingent.employee!!.id!!),
            Absence(absenceDate = LocalDate.of(year, 2, 5), employeeId = contingent.employee!!.id!!),
            Absence(absenceDate = LocalDate.of(year, 1, 6), employeeId = contingent.employee!!.id!! + 1)
        )

        // When
        val result = contingentCalculationService.countAbsenceDaysBy(year, 1, contingent, absences)

        // Then
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun countAbsenceDaysInContingentForYear_countsOnlyInsideRange() {
        // Given
        val year = 2024
        val contingent = contingentWith(
            start = LocalDate.of(year, 1, 1),
            end = LocalDate.of(year, 1, 10)
        )
        val absences = listOf(
            Absence(absenceDate = LocalDate.of(year, 1, 2), employeeId = contingent.employee!!.id!!),
            Absence(absenceDate = LocalDate.of(year, 1, 5), employeeId = contingent.employee!!.id!!),
            Absence(absenceDate = LocalDate.of(year, 1, 11), employeeId = contingent.employee!!.id!!)
        )

        // When
        val result = contingentCalculationService.countAbsenceDaysInContingentForYear(year, contingent, absences)

        // Then
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun isContingentInYearMonth_insideRange_returnsTrue() {
        // Given
        val contingent = contingentWith(start = LocalDate.of(2024, 1, 1), end = LocalDate.of(2024, 3, 1))

        // When
        val result = contingentCalculationService.isContingentInYearMonth(2024, 2, contingent)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun isContingentInYearMonth_outsideRange_returnsFalse() {
        // Given
        val contingent = contingentWith(start = LocalDate.of(2024, 3, 1))

        // When
        val result = contingentCalculationService.isContingentInYearMonth(2024, 2, contingent)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun calculateContingentMinutesForWorkdayBy_weekend_returnsZero() {
        // Given
        val date = LocalDate.of(2024, 1, 6)
        val contingents = listOf(contingentWith(start = LocalDate.of(2024, 1, 1)))

        // When
        val result = contingentCalculationService.calculateContingentMinutesForWorkdayBy(date, contingents)

        // Then
        assertThat(result).isEqualTo(0.0)
    }

    @Test
    fun calculateContingentMinutesForWorkdayBy_matchingContingent_returnsDailyMinutes() {
        // Given
        val date = LocalDate.of(2024, 1, 2)
        val contingents = listOf(contingentWith(
            start = LocalDate.of(2024, 1, 1),
            end = LocalDate.of(2024, 1, 31),
            weeklyHours = 10.0
        ))

        // When
        val result = contingentCalculationService.calculateContingentMinutesForWorkdayBy(date, contingents)

        // Then
        assertThat(result).isEqualTo(120.0)
    }

    @Test
    fun calculateContingentMinutesFor_rangeOfWorkdays_returnsCeiledMinutes() {
        // Given
        val start = LocalDate.of(2024, 1, 2)
        val end = LocalDate.of(2024, 1, 3)
        val contingents = listOf(contingentWith(
            start = LocalDate.of(2024, 1, 1),
            end = LocalDate.of(2024, 1, 31),
            weeklyHours = 10.0
        ))

        // When
        val result = contingentCalculationService.calculateContingentMinutesFor(start, end, contingents)

        // Then
        assertThat(result).isEqualTo(240)
    }

    private fun contingentWith(
        start: LocalDate = LocalDate.of(2024, 1, 1),
        end: LocalDate? = null,
        weeklyHours: Double = 7.0,
        employeeId: Long = 1
    ): Contingent = Contingent(
        id = 1,
        start = start,
        end = end,
        weeklyServiceHours = weeklyHours,
        employee = Employee(id = employeeId)
    )
}
