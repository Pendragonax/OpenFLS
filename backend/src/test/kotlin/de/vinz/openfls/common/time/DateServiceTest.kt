package de.vinz.openfls.common.time

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

class DateServiceTest {
    @Test
    fun isDateInAssistancePlan_firstDay_true() {
        // Given
        val start = LocalDate.of(2023, 1, 1);
        val end = LocalDate.of(2023, 3, 31);
        val checkDate = LocalDate.of(2023, 1, 1);

        // When
        val isInside = DateService.isDateInAssistancePlan(checkDate, start, end);

        // Then
        assertThat(isInside).isTrue();
    }

    @Test
    fun isDateInAssistancePlan_lastDay_true() {
        // Given
        val start = LocalDate.of(2023, 1, 1);
        val end = LocalDate.of(2023, 3, 31);
        val checkDate = LocalDate.of(2023, 3, 31);

        // When
        val isInside = DateService.isDateInAssistancePlan(checkDate, start, end);

        // Then
        assertThat(isInside).isTrue();
    }

    @Test
    fun isDateInAssistancePlan_inBetween_true() {
        // Given
        val start = LocalDate.of(2023, 1, 1);
        val end = LocalDate.of(2023, 3, 31);
        val checkDate = LocalDate.of(2023, 2, 12);

        // When
        val isInside = DateService.isDateInAssistancePlan(checkDate, start, end);

        // Then
        assertThat(isInside).isTrue();
    }

    @Test
    fun isDateInAssistancePlan_oneDayBefore_false() {
        // Given
        val start = LocalDate.of(2023, 1, 1);
        val end = LocalDate.of(2023, 3, 31);
        val checkDate = LocalDate.of(2022, 12, 31);

        // When
        val isInside = DateService.isDateInAssistancePlan(checkDate, start, end);

        // Then
        assertThat(isInside).isFalse();
    }

    @Test
    fun isDateInAssistancePlan_oneDayAfter_false() {
        // Given
        val start = LocalDate.of(2023, 1, 1);
        val end = LocalDate.of(2023, 3, 31);
        val checkDate = LocalDate.of(2023, 4, 1);

        // When
        val isInside = DateService.isDateInAssistancePlan(checkDate, start, end);

        // Then
        assertThat(isInside).isFalse();
    }

    @ParameterizedTest
    @CsvSource("2023, 1, false", "2023, 2, true", "2023, 12, false", "2023, 6, true", "2022, 6, false")
    fun containsStartAndEndASpecificYearMonth(year: Int, month: Int, expected: Boolean) {
        // Given
        val start = LocalDate.of(2023, 2, 1)
        val end = LocalDate.of(2023, 11, 30)
        val checkYearMonth = YearMonth.of(year, month)

        // When
        val isInside = DateService.containsStartAndEndASpecificYearMonth(start, end, checkYearMonth)

        // Then
        assertThat(isInside).isEqualTo(expected);
    }

    @Test
    fun countDaysOfAssistancePlan_fullMonth_correctAmount() {
        // Given
        val start = LocalDate.of(2023, 1, 1);
        val end = LocalDate.of(2023, 1, 31);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, 1, start, end);

        // Then
        assertThat(numberOfDays).isEqualTo(31);
    }

    @Test
    fun countDaysOfAssistancePlan_inBetweenMonth_correctAmount() {
        // Given
        val start = LocalDate.of(2023, 1, 5);
        val end = LocalDate.of(2023, 1, 15);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, 1, start, end);

        // Then
        assertThat(numberOfDays).isEqualTo(11);
    }

    @Test
    fun countDaysOfAssistancePlan_firstDayOfMonth_correctAmount() {
        // Given
        val start = LocalDate.of(2022, 12, 1);
        val end = LocalDate.of(2023, 1, 1);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, 1, start, end);

        // Then
        assertThat(numberOfDays).isEqualTo(1);
    }

    @Test
    fun countDaysOfAssistancePlan_lastDayOfMonth_correctAmount() {
        // Given
        val start = LocalDate.of(2023, 1, 31);
        val end = LocalDate.of(2023, 2, 12);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, 1, start, end);

        // Then
        assertThat(numberOfDays).isEqualTo(1);
    }

    @Test
    fun countDaysOfAssistancePlan_fullYear_correctAmount() {
        // Given
        val start = LocalDate.of(2023, 1, 1);
        val end = LocalDate.of(2023, 12, 31);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, start, end);

        // Then
        assertThat(numberOfDays).isEqualTo(Year.of(2023).length().toLong());
    }

    @Test
    fun countDaysOfAssistancePlan_startOfTheYear_correctAmount() {
        // Given
        val start = LocalDate.of(2022, 1, 1);
        val end = LocalDate.of(2023, 2, 1);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, start, end);

        // Then
        assertThat(numberOfDays).isEqualTo(32);
    }

    @Test
    fun countDaysOfAssistancePlan_endOfTheYear_correctAmount() {
        // Given
        val start = LocalDate.of(2023, 11, 30);
        val end = LocalDate.of(2024, 1, 1);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, start, end);

        // Then
        assertThat(numberOfDays).isEqualTo(32);
    }

    @Test
    fun countDaysOfAssistancePlan_inBetween_correctAmount() {
        // Given
        val start = LocalDate.of(2023, 3, 1);
        val end = LocalDate.of(2023, 4, 30);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, start, end);

        // Then
        assertThat(numberOfDays).isEqualTo(61);
    }

    @Test
    fun countDaysOfYearBetweenStartAndEnd_sameDay_returnsOne() {
        // Given
        val start = LocalDate.of(2024, 6, 15)
        val end = LocalDate.of(2024, 6, 15)

        // When
        val numberOfDays = DateService.countDaysOfYearBetweenStartAndEnd(start, end)

        // Then
        assertThat(numberOfDays).isEqualTo(1)
    }

    @Test
    fun countDaysOfYearBetweenStartAndEnd_leapDayRange_returnsCorrectAmount() {
        // Given
        val start = LocalDate.of(2024, 2, 28)
        val end = LocalDate.of(2024, 3, 1)

        // When
        val numberOfDays = DateService.countDaysOfYearBetweenStartAndEnd(start, end)

        // Then
        assertThat(numberOfDays).isEqualTo(3)
    }

    @Test
    fun countDaysOfMonthAndYearBetweenStartAndEnd_inBetween_correctAmount() {
        // Given
        val start = LocalDate.of(2023, 1, 1);
        val end = LocalDate.of(2023, 1, 31);

        // When
        val numberOfDays = DateService.countDaysOfMonthAndYearBetweenStartAndEnd(2023, 1, start, end)

        // Then
        assertThat(numberOfDays).isEqualTo(31);
    }
}
