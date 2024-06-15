package de.vinz.openfls.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TimeDoubleServiceTest {
    @Test
    fun roundDoubleToTwoDigits_valid_rounded() {
        // Given
        val value = 3.4645745452453;

        // When
        val result = TimeDoubleService.roundDoubleToTwoDigits(value);

        // Then
        assertThat(result).isEqualTo(3.46);
    }

    @Test
    fun convertDoubleToTimeDouble_30Minutes_timeDouble() {
        // Given
        val value = 1.5;

        // When
        val timeDouble = TimeDoubleService.convertDoubleToTimeDouble(value);

        // Then
        assertThat(timeDouble).isEqualTo(1.3)
    }

    @Test
    fun convertDoubleToTimeDouble_45Minutes_timeDouble() {
        // Given
        val value = 1.75;

        // When
        val timeDouble = TimeDoubleService.convertDoubleToTimeDouble(value);

        // Then
        assertThat(timeDouble).isEqualTo(1.45)
    }
}