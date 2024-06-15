package de.vinz.openfls.services

import de.vinz.openfls.domains.overviews.dtos.AssistancePlanOverviewDTO
import de.vinz.openfls.domains.overviews.exceptions.CsvCreationFailedException
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintWriter

class CsvService {
    companion object {
        fun <T> getCsvString(header: Array<String>, data: Array<Array<T>>, separator: String = ";"): String {
            val rows = data.map {row -> row.joinToString(separator = separator) { "\"${it}\"" } }.toMutableList()
            rows.add(0, header.joinToString(separator = separator) { "\"${it}\"" })
            return rows.joinToString(separator = System.lineSeparator())
        }

        @Throws(CsvCreationFailedException::class)
        fun getCsvFileStream(overviewData: List<AssistancePlanOverviewDTO>): ByteArrayInputStream {
            val headerList = mutableListOf("Nachname", "Vorname", "Hilfeplan-Start", "Hilfeplan-Ende", "Kostenträger-ID")
            headerList.addAll(overviewData[0].values.mapIndexed{ index, _ -> if (index == 0) "Gesamt" else "$index" })
            val header: Array<String> = headerList.toTypedArray()

            val csvFormat = CSVFormat
                    .DEFAULT
                    .builder()
                    .setHeader(*header)
                    .setDelimiter(";")
                    .build()
            try {
                val out = ByteArrayOutputStream()
                val printer = CSVPrinter(PrintWriter(out), csvFormat)
                overviewData.map { convertToArray(it) }
                        .forEach { printer.printRecord(*it)}
                printer.flush()
                return ByteArrayInputStream(out.toByteArray())
            } catch (ex: Exception) {
                throw CsvCreationFailedException()
            }
        }

        private fun convertToArray(overview: AssistancePlanOverviewDTO): Array<String> {
            val result = mutableListOf(
                    overview.clientDto.lastName,
                    overview.clientDto.firstName,
                    overview.assistancePlanDto.start.toString(),
                    overview.assistancePlanDto.end.toString(),
                    overview.assistancePlanDto.sponsorId.toString())
            result.addAll(overview.values.map { value -> "${NumberService.roundDoubleToTwoDigits(value)}" })
            return result.toTypedArray()
        }
    }
}