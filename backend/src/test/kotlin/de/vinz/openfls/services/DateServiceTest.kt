package de.vinz.openfls.services

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.exceptions.YearOutOfRangeException
import de.vinz.openfls.services.models.DateRangeArgument
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.util.stream.Stream

class DateServiceTest {
    @Test
    fun isDateInAssistancePlan_firstDay_true() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2023, 1, 1);
        assistancePlanDto.end = LocalDate.of(2023, 3, 31);
        val checkDate = LocalDate.of(2023, 1, 1);

        // When
        val isInside = DateService.isDateInAssistancePlan(checkDate, assistancePlanDto);

        // Then
        assertThat(isInside).isTrue();
    }

    @Test
    fun isDateInAssistancePlan_lastDay_true() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2023, 1, 1);
        assistancePlanDto.end = LocalDate.of(2023, 3, 31);
        val checkDate = LocalDate.of(2023, 3, 31);

        // When
        val isInside = DateService.isDateInAssistancePlan(checkDate, assistancePlanDto);

        // Then
        assertThat(isInside).isTrue();
    }

    @Test
    fun isDateInAssistancePlan_inBetween_true() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2023, 1, 1);
        assistancePlanDto.end = LocalDate.of(2023, 3, 31);
        val checkDate = LocalDate.of(2023, 2, 12);

        // When
        val isInside = DateService.isDateInAssistancePlan(checkDate, assistancePlanDto);

        // Then
        assertThat(isInside).isTrue();
    }

    @Test
    fun isDateInAssistancePlan_oneDayBefore_false() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2023, 1, 1);
        assistancePlanDto.end = LocalDate.of(2023, 3, 31);
        val checkDate = LocalDate.of(2022, 12, 31);

        // When
        val isInside = DateService.isDateInAssistancePlan(checkDate, assistancePlanDto);

        // Then
        assertThat(isInside).isFalse();
    }

    @Test
    fun isDateInAssistancePlan_oneDayAfter_false() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2023, 1, 1);
        assistancePlanDto.end = LocalDate.of(2023, 3, 31);
        val checkDate = LocalDate.of(2023, 4, 1);

        // When
        val isInside = DateService.isDateInAssistancePlan(checkDate, assistancePlanDto);

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
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2023, 1, 1);
        assistancePlanDto.end = LocalDate.of(2023, 1, 31);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, 1, assistancePlanDto);

        // Then
        assertThat(numberOfDays).isEqualTo(31);
    }

    @Test
    fun countDaysOfAssistancePlan_inBetweenMonth_correctAmount() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2023, 1, 5);
        assistancePlanDto.end = LocalDate.of(2023, 1, 15);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, 1, assistancePlanDto);

        // Then
        assertThat(numberOfDays).isEqualTo(11);
    }

    @Test
    fun countDaysOfAssistancePlan_firstDayOfMonth_correctAmount() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2022, 12, 1);
        assistancePlanDto.end = LocalDate.of(2023, 1, 1);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, 1, assistancePlanDto);

        // Then
        assertThat(numberOfDays).isEqualTo(1);
    }

    @Test
    fun countDaysOfAssistancePlan_lastDayOfMonth_correctAmount() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2023, 1, 31);
        assistancePlanDto.end = LocalDate.of(2023, 2, 12);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, 1, assistancePlanDto);

        // Then
        assertThat(numberOfDays).isEqualTo(1);
    }

    @Test
    fun countDaysOfAssistancePlan_fullYear_correctAmount() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2023, 1, 1);
        assistancePlanDto.end = LocalDate.of(2023, 12, 31);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, assistancePlanDto);

        // Then
        assertThat(numberOfDays).isEqualTo(Year.of(2023).length().toLong());
    }

    @Test
    fun countDaysOfAssistancePlan_startOfTheYear_correctAmount() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2022, 1, 1);
        assistancePlanDto.end = LocalDate.of(2023, 2, 1);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, assistancePlanDto);

        // Then
        assertThat(numberOfDays).isEqualTo(32);
    }

    @Test
    fun countDaysOfAssistancePlan_endOfTheYear_correctAmount() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2023, 11, 30);
        assistancePlanDto.end = LocalDate.of(2024, 1, 1);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, assistancePlanDto);

        // Then
        assertThat(numberOfDays).isEqualTo(32);
    }

    @Test
    fun countDaysOfAssistancePlan_inBetween_correctAmount() {
        // Given
        val assistancePlanDto = AssistancePlanDto();
        assistancePlanDto.start = LocalDate.of(2023, 3, 1);
        assistancePlanDto.end = LocalDate.of(2023, 4, 30);

        // When
        val numberOfDays = DateService.countDaysOfAssistancePlan(2023, assistancePlanDto);

        // Then
        assertThat(numberOfDays).isEqualTo(61);
    }

    @Test
    fun getStartAndEndInYear_inBetween_correctAmount() {
        // Given
        val start = LocalDate.of(2023, 1, 1)
        val end = LocalDate.of(2023, 1, 31)

        // When
        val numberOfDays = DateService.getStartAndEndInYear(2023, start, end)

        // Then
        assertThat(numberOfDays.first).isEqualTo(start)
        assertThat(numberOfDays.second).isEqualTo(end)
    }

    @Test
    fun getStartAndEndInYear_startBeforeEndInBetween_correctAmount() {
        // Given
        val start = LocalDate.of(2022, 1, 1)
        val end = LocalDate.of(2023, 1, 31)

        // When
        val numberOfDays = DateService.getStartAndEndInYear(2023, start, end)

        // Then
        assertThat(numberOfDays.first).isEqualTo(LocalDate.of(2023, 1, 1))
        assertThat(numberOfDays.second).isEqualTo(end)
    }

    @Test
    fun getStartAndEndInYear_startInBetweenEndAfter_correctAmount() {
        // Given
        val start = LocalDate.of(2023, 1, 26)
        val end = LocalDate.of(2024, 1, 31)

        // When
        val numberOfDays = DateService.getStartAndEndInYear(2023, start, end)

        // Then
        assertThat(numberOfDays.first).isEqualTo(start)
        assertThat(numberOfDays.second).isEqualTo(LocalDate.of(2023, 12, 31))
    }

    @Test
    fun getStartAndEndInYear_before_ThrowException() {
        // Given
        val year = 2023
        val start = LocalDate.of(2022, 1, 1)
        val end = LocalDate.of(2022, 12, 31)

        // When
        assertThatCode { DateService.getStartAndEndInYear(year, start, end) }
                .isInstanceOf(YearOutOfRangeException::class.java)
    }

    @Test
    fun getStartAndEndInYear_after_ThrowException() {
        // Given
        val year = 2021
        val start = LocalDate.of(2022, 1, 1)
        val end = LocalDate.of(2022, 12, 31)

        // When
        assertThatCode { DateService.getStartAndEndInYear(year, start, end) }
                .isInstanceOf(YearOutOfRangeException::class.java)
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

    @ParameterizedTest
    @MethodSource("dateRangeProvider")
    fun countDaysOfMonthAndYearBetweenStartAndEnd_endInBetween_correctAmount(arguments: DateRangeArgument) {
        // Given

        // When
        val numberOfDays = DateService.countDaysOfMonthAndYearBetweenStartAndEnd(
                arguments.year, arguments.month, arguments.start, arguments.end)

        // Then
        assertThat(numberOfDays).isEqualTo(arguments.expectedDays);
    }

    companion object {
        @JvmStatic
        fun dateRangeProvider(): Stream<DateRangeArgument> {
            return Stream.of(
                    DateRangeArgument(
                            LocalDate.of(2022, 1, 1),
                            LocalDate.of(2022, 1, 31),
                            2022,
                            1,
                            31),
                    DateRangeArgument(
                            LocalDate.of(2022, 1, 1),
                            LocalDate.of(2022, 1, 11),
                            2022,
                            1,
                            11),
                    DateRangeArgument(
                            LocalDate.of(2022, 1, 5),
                            LocalDate.of(2022, 1, 11),
                            2022,
                            1,
                            7),
                    DateRangeArgument(
                            LocalDate.of(2021, 1, 5),
                            LocalDate.of(2022, 1, 11),
                            2022,
                            1,
                            11),
                    DateRangeArgument(
                            LocalDate.of(2022, 1, 5),
                            LocalDate.of(2023, 1, 11),
                            2022,
                            1,
                            27),
                    DateRangeArgument(
                            LocalDate.of(2023, 1, 5),
                            LocalDate.of(2023, 1, 11),
                            2022,
                            1,
                            0),
                    DateRangeArgument(
                            LocalDate.of(2022, 1, 5),
                            LocalDate.of(2022, 1, 11),
                            2023,
                            1,
                            0))
        }
    }
}