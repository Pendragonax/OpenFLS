package de.vinz.openfls.services

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class TimeDoubleServiceTest {
    @ParameterizedTest
    @MethodSource("roundDoubleToTwoDigitsCases")
    fun roundDoubleToTwoDigits_fractionalDouble_expectedRounded(value: Double, expected: Double) {
        // Given
        val input = value

        // When
        val result = TimeDoubleService.roundDoubleToTwoDigits(input)

        // Then
        assertThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("convertDoubleToTimeDoubleCases")
    fun convertDoubleToTimeDouble_nonSmoothDouble_expectedTimeDouble(value: Double, expected: Double) {
        // Given
        val input = value

        // When
        val result = TimeDoubleService.convertDoubleToTimeDouble(input)

        // Then
        assertThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("convertTimeDoubleToDoubleCases")
    fun convertTimeDoubleToDouble_validTimeDouble_expectedDecimalHours(value: Double, expected: Double) {
        // Given
        val input = value

        // When
        val result = TimeDoubleService.convertTimeDoubleToDouble(input)

        // Then
        assertThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("sumTimeDoublesCases")
    fun sumTimeDoubles_validTimeDoubles_expectedSummedTimeDouble(sum1: Double, sum2: Double, expected: Double) {
        // Given
        val input1 = sum1
        val input2 = sum2

        // When
        val result = TimeDoubleService.sumTimeDoubles(input1, input2)

        // Then
        assertThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("diffTimeDoublesCases")
    fun diffTimeDoubles_validTimeDoubles_expectedDifference(sum1: Double, sum2: Double, expected: Double) {
        // Given
        val input1 = sum1
        val input2 = sum2

        // When
        val result = TimeDoubleService.diffTimeDoubles(input1, input2)

        // Then
        assertThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("invalidTimeDoubleCases")
    fun sumTimeDoubles_invalidMinutePart_expectedIllegalArgument(sum1: Double, sum2: Double) {
        // Given
        val input1 = sum1
        val input2 = sum2

        // When / Then
        assertThatThrownBy { TimeDoubleService.sumTimeDoubles(input1, input2) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Ungültige Minutenanteil")
    }

    companion object {
        @JvmStatic
        fun roundDoubleToTwoDigitsCases(): Stream<Arguments> = Stream.of(
            Arguments.of(3.4645745452453, 3.46),
            Arguments.of(1.005, 1.01),
            Arguments.of(2.335, 2.34),
            Arguments.of(0.1 + 0.2, 0.30)
        )

        @JvmStatic
        fun convertDoubleToTimeDoubleCases(): Stream<Arguments> = Stream.of(
            Arguments.of(1.5, 1.30),
            Arguments.of(1.75, 1.45),
            Arguments.of(1.3333333333, 1.20),
            Arguments.of(1.995, 2.00),
            Arguments.of(-1.5, -1.30),
            Arguments.of(-4.83, -4.5)
        )

        @JvmStatic
        fun convertTimeDoubleToDoubleCases(): Stream<Arguments> = Stream.of(
            Arguments.of(1.30, 1.50),
            Arguments.of(1.45, 1.75),
            Arguments.of(2.00, 2.00),
            Arguments.of(0.59, 0.98),
            Arguments.of(-1.30, -1.50)
        )

        @JvmStatic
        fun sumTimeDoublesCases(): Stream<Arguments> = Stream.of(
            Arguments.of(1.40, 2.35, 4.15),
            Arguments.of(0.10, 0.55, 1.05),
            Arguments.of(10.59, 0.01, 11.00),
            Arguments.of(-1.20, 0.50, -0.30)
        )

        @JvmStatic
        fun diffTimeDoublesCases(): Stream<Arguments> = Stream.of(
            Arguments.of(2.00, 1.30, 0.30),
            Arguments.of(1.00, 0.45, 0.15),
            Arguments.of(0.30, 1.00, -0.30),
            Arguments.of(10.00, 0.59, 9.01)
        )

        @JvmStatic
        fun invalidTimeDoubleCases(): Stream<Arguments> = Stream.of(
            Arguments.of(1.75, 0.10),
            Arguments.of(0.60, 0.10),
            Arguments.of(2.99, 0.01)
        )
    }
}
