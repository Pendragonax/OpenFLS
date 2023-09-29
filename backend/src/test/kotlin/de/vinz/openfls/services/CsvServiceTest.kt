package de.vinz.openfls.services

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

class CsvServiceTest {
    @Test
    fun getCsvString_valid_correctString() {
        // Given
        val header = arrayOf("First", "Second", "Third")
        val data = arrayOf(
                arrayOf("11", "12", "13"),
                arrayOf("21", "22", "23"),
                arrayOf("31", "32", "33"),
                arrayOf("41", "42", "43"),
                arrayOf("51", "52", "53"))
        val separator = ";"
        val expected = "\"${header[0]}\";\"${header[1]}\";\"${header[2]}\"${System.lineSeparator()}" +
                "\"${data[0][0]}\";\"${data[0][1]}\";\"${data[0][2]}\"${System.lineSeparator()}" +
                "\"${data[1][0]}\";\"${data[1][1]}\";\"${data[1][2]}\"${System.lineSeparator()}" +
                "\"${data[2][0]}\";\"${data[2][1]}\";\"${data[2][2]}\"${System.lineSeparator()}" +
                "\"${data[3][0]}\";\"${data[3][1]}\";\"${data[3][2]}\"${System.lineSeparator()}" +
                "\"${data[4][0]}\";\"${data[4][1]}\";\"${data[4][2]}\""

        // When
        val result = CsvService.getCsvString(header, data, separator)

        // Then
        assertThat(result).isEqualTo(expected)
    }
}