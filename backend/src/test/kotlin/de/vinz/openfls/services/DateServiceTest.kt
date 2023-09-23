package de.vinz.openfls.services

import de.vinz.openfls.dtos.AssistancePlanDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Year

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
}