package de.vinz.openfls.domains.hourReports.service

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanEditResponse
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanHourResponse
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.clients.service.ClientService
import de.vinz.openfls.domains.clients.dto.ClientNameDto
import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor
import de.vinz.openfls.domains.hourCorridors.service.HourCorridorService
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.hourReports.dto.HourReportRowResponse
import de.vinz.openfls.domains.hourReports.dto.HourReportResult
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.services.service.ServiceService
import de.vinz.openfls.services.TimeDoubleService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.YearMonth

class HourReportServiceTest {

    private val accessService: AccessService = mock()
    private val serviceService: ServiceService = mock()
    private val assistancePlanService: AssistancePlanService = mock()
    private val hourCorridorService: HourCorridorService = mock()
    private val clientService: ClientService = mock()
    private lateinit var hourReportService: HourReportService

    @BeforeEach
    fun setup() {
        hourReportService = HourReportService(
            accessService, serviceService, assistancePlanService, hourCorridorService, clientService
        )
        whenever(accessService.isAdmin()).thenReturn(true)
        whenever(serviceService.getAllEntitiesByYearAndMonthAndHourTypeIdAndInstitutionIdAndSponsorId(any(), any(), any(), any(), any()))
            .thenReturn(emptyList())
    }

    private fun stubContext(
        year: Int,
        institutionId: Long?,
        sponsorId: Long?,
        plans: List<AssistancePlanEditResponse>,
        clients: List<ClientNameDto>
    ) {
        whenever(assistancePlanService.getAllEditResponsesByYearAndInstitutionIdAndSponsorId(year, institutionId, sponsorId))
            .thenReturn(plans)
        whenever(clientService.getAllClientNameDtos()).thenReturn(clients)
    }

    private fun success(result: HourReportResult): List<HourReportRowResponse> {
        check(result is HourReportResult.Success) { "expected Success but was $result" }
        return result.rows
    }

    @Test
    fun getApprovedHoursReport_invalidMonth_returnsInvalidTimeRange() {
        val result = hourReportService.getApprovedHoursReport(2023, 13, 1L, null, null)

        assertThat(result).isEqualTo(HourReportResult.InvalidTimeRange)
    }

    @Test
    fun getApprovedHoursReport_negativeYear_returnsInvalidTimeRange() {
        val result = hourReportService.getApprovedHoursReport(-1, null, 1L, null, null)

        assertThat(result).isEqualTo(HourReportResult.InvalidTimeRange)
    }

    @Test
    fun getApprovedHoursReport_noAreaAndNotAdmin_returnsForbidden() {
        whenever(accessService.isAdmin()).thenReturn(false)

        val result = hourReportService.getApprovedHoursReport(2024, null, 1L, null, null)

        assertThat(result).isEqualTo(HourReportResult.Forbidden)
    }

    @Test
    fun getApprovedHoursReport_areaWithoutReadAccess_returnsForbidden() {
        whenever(accessService.isAdmin()).thenReturn(false)
        whenever(accessService.canReadEntries(9L)).thenReturn(false)

        val result = hourReportService.getApprovedHoursReport(2024, null, 1L, 9L, null)

        assertThat(result).isEqualTo(HourReportResult.Forbidden)
    }

    @Test
    fun getApprovedHoursReport_monthly_withArchivedClient_includesArchivedClientRowAndTotals() {
        val year = 2024
        val month = 2
        val hourTypeId = 7L

        val activeClient = clientDto(1L, "Aktiv", "Alpha")
        val archivedClient = clientDto(2L, "Archiv", "Beta")
        val activePlan = planDto(11L, activeClient.id, year, month, hourTypeId)
        val archivedPlan = planDto(22L, archivedClient.id, year, month, hourTypeId)
        stubContext(year, null, null, listOf(activePlan, archivedPlan), listOf(activeClient, archivedClient))

        val result = success(hourReportService.getApprovedHoursReport(year, month, hourTypeId, null, null))

        val archivedRow = result.first { it.clientDto.id == archivedClient.id }
        val allRow = result.first { it.clientDto.id == 0L }

        assertThat(archivedRow.clientDto.lastName).isEqualTo("Beta")
        assertThat(archivedRow.values[0]).isEqualTo(29.0)
        assertThat(allRow.values[0]).isEqualTo(58.0)
    }

    @Test
    fun getApprovedHoursReport_yearly_withArchivedClient_includesArchivedClientRowAndTotals() {
        val year = 2024
        val hourTypeId = 7L

        val activeClient = clientDto(1L, "Aktiv", "Alpha")
        val archivedClient = clientDto(2L, "Archiv", "Beta")
        val activePlan = planDto(11L, activeClient.id, year, null, hourTypeId)
        val archivedPlan = planDto(22L, archivedClient.id, year, null, hourTypeId)
        stubContext(year, null, null, listOf(activePlan, archivedPlan), listOf(activeClient, archivedClient))

        val result = success(hourReportService.getApprovedHoursReport(year, null, hourTypeId, null, null))

        val archivedRow = result.first { it.clientDto.id == archivedClient.id }
        val allRow = result.first { it.clientDto.id == 0L }

        assertThat(archivedRow.clientDto.lastName).isEqualTo("Beta")
        assertThat(archivedRow.values[0]).isEqualTo(366.0)
        assertThat(allRow.values[0]).isEqualTo(732.0)
    }

    @Test
    fun getApprovedHoursReport_yearlyWithCorridorPlan_usesCorridorMean() {
        val year = 2024
        val hourTypeId = 7L

        val client = clientDto(1L, "Max", "Muster")
        val corridorPlan = planDto(11L, client.id, year, null, hourTypeId, corridor = true)
        val corridor = corridorEntity(5L, hourTypeId, 300, 600)
        whenever(hourCorridorService.getAllEntitiesByIds(listOf(5L))).thenReturn(listOf(corridor))
        stubContext(year, null, null, listOf(corridorPlan), listOf(client))

        val result = success(hourReportService.getApprovedHoursReport(year, null, hourTypeId, null, null))

        val row = result.first { it.assistancePlanDto.id == corridorPlan.id }

        // Korridor-Mittelwert: (300+600)/2 Wochenminuten -> Stunden/Tag, je Monat auf Tage hochgerechnet
        val hoursPerDay = (300 + 600) / 2.0 / 7.0 / 60.0
        val monthlyValues = (1..12).map { month ->
            TimeDoubleService.convertDoubleToTimeDouble(hoursPerDay * YearMonth.of(year, month).lengthOfMonth())
        }
        val expectedTotal = monthlyValues.fold(0.0) { acc, v -> TimeDoubleService.sumTimeDoubles(acc, v) }

        assertThat(row.values[2]).isCloseTo(monthlyValues[1], within(0.0001))
        assertThat(row.values[0]).isCloseTo(expectedTotal, within(0.0001))
    }

    @Test
    fun getDifferenceHoursReport_yearlyWithCorridorPlan_usesCorridorBounds() {
        val year = 2024
        val hourTypeId = 7L
        val weeklyMinutesFrom = 300
        val weeklyMinutesTill = 600

        val client = clientDto(1L, "Max", "Muster")
        val corridorPlan = planDto(11L, client.id, year, null, hourTypeId, corridor = true)
        val corridor = corridorEntity(5L, hourTypeId, weeklyMinutesFrom, weeklyMinutesTill)
        whenever(hourCorridorService.getAllEntitiesByIds(listOf(5L))).thenReturn(listOf(corridor))
        stubContext(year, null, null, listOf(corridorPlan), listOf(client))
        whenever(serviceService.getAllEntitiesByYearAndMonthAndHourTypeIdAndInstitutionIdAndSponsorId(year, null, hourTypeId, null, null))
            .thenReturn(listOf(service(11L, LocalDate.of(2024, 2, 10), 60)))

        val result = success(hourReportService.getDifferenceHoursReport(year, null, hourTypeId, null, null))

        val row = result.first { it.assistancePlanDto.id == corridorPlan.id }

        // Nachbau der Korridor-Differenz je Monat: min(0, executed-from) bzw. max(0, executed-to), danach dieselbe
        // HH.MM-Rundung/Summierung wie in der Produktion (TimeDoubleService), damit keine Rundungs-Artefakte von Hand
        // nachgerechnet werden müssen.
        val executedByMonth = (1..12).associateWith { month -> if (month == 2) 1.0 else 0.0 }
        val monthlyDiffs = (1..12).map { month ->
            val days = YearMonth.of(year, month).lengthOfMonth()
            val approvedFrom = TimeDoubleService.roundDoubleToTwoDigits((weeklyMinutesFrom / 7.0) * days / 60.0)
            val approvedTo = TimeDoubleService.roundDoubleToTwoDigits((weeklyMinutesTill / 7.0) * days / 60.0)
            val executed = executedByMonth.getValue(month)
            val diff = when {
                executed < approvedFrom -> executed - approvedFrom
                executed > approvedTo -> executed - approvedTo
                else -> 0.0
            }
            TimeDoubleService.convertDoubleToTimeDouble(diff)
        }
        val expectedFeb = monthlyDiffs[1]
        val expectedTotal = monthlyDiffs.fold(0.0) { acc, v -> TimeDoubleService.sumTimeDoubles(acc, v) }

        assertThat(row.values[2]).isCloseTo(expectedFeb, within(0.0001))
        assertThat(row.values[0]).isCloseTo(expectedTotal, within(0.0001))
    }

    private fun clientDto(id: Long, firstName: String, lastName: String): ClientNameDto {
        return ClientNameDto(id = id, firstName = firstName, lastName = lastName)
    }

    private fun planDto(
        id: Long,
        clientId: Long,
        year: Int,
        month: Int?,
        hourTypeId: Long,
        corridor: Boolean = false
    ): AssistancePlanEditResponse {
        val plan = AssistancePlanEditResponse().apply {
            this.id = id
            this.clientId = clientId
            this.start = if (month != null) LocalDate.of(year, month, 1) else LocalDate.of(year, 1, 1)
            this.end = if (month != null) LocalDate.of(year, month, 1).plusMonths(1).minusDays(1) else LocalDate.of(year, 12, 31)
            if (corridor) {
                this.hourMode = AssistancePlanHourMode.CORRIDOR
                this.hourCorridorId = 5
            }
        }

        plan.hours = listOf(
            AssistancePlanHourResponse(assistancePlanId = id, hourTypeId = hourTypeId, weeklyMinutes = 420)
        )

        return plan
    }

    private fun corridorEntity(id: Long, hourTypeId: Long, weeklyMinutesFrom: Int, weeklyMinutesTill: Int): HourCorridor {
        return HourCorridor(
            id = id,
            title = "Korridor",
            weeklyMinutesFrom = weeklyMinutesFrom,
            weeklyMinutesTill = weeklyMinutesTill,
            hourType = HourType(id = hourTypeId, title = "Korridor")
        )
    }

    private fun service(assistancePlanId: Long, start: LocalDate, minutes: Int) =
        de.vinz.openfls.domains.services.entity.Service(
            start = start.atTime(8, 0),
            end = start.atTime(8, 0).plusMinutes(minutes.toLong()),
            minutes = minutes,
            assistancePlan = AssistancePlan(id = assistancePlanId)
        )
}
