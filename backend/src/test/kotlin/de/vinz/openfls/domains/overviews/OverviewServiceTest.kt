package de.vinz.openfls.domains.overviews

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourDto
import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.hourCorridors.HourCorridor
import de.vinz.openfls.domains.hourCorridors.HourCorridorRepository
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.exceptions.IllegalTimeException
import de.vinz.openfls.exceptions.UserNotAllowedException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.modelmapper.ModelMapper
import java.time.LocalDateTime
import java.time.LocalDate
import java.util.Optional

class OverviewServiceTest {

    private lateinit var overviewService: OverviewService
    private lateinit var accessService: AccessService
    private lateinit var serviceRepository: ServiceRepository
    private lateinit var assistancePlanRepository: AssistancePlanRepository
    private lateinit var hourCorridorRepository: HourCorridorRepository
    private lateinit var clientRepository: ClientRepository
    private lateinit var modelMapper: ModelMapper

    @BeforeEach
    fun setup() {
        accessService = mock()
        serviceRepository = mock()
        assistancePlanRepository = mock()
        hourCorridorRepository = mock()
        clientRepository = mock()
        modelMapper = mock()

        overviewService = OverviewService(
            accessService, serviceRepository, assistancePlanRepository, hourCorridorRepository, clientRepository, modelMapper
        )
    }

    @Test
    fun checkYearMonth_whenValidYearAndMonth_thenNoException() {
        assertThatCode { overviewService.checkYearMonth(2023, 5) }
            .doesNotThrowAnyException()
    }

    @Test
    fun checkYearMonth_whenInvalidMonth_thenThrowIllegalTimeException() {
        assertThrows(IllegalTimeException::class.java) {
            overviewService.checkYearMonth(2023, 13)
        }
        assertThrows(IllegalTimeException::class.java) {
            overviewService.checkYearMonth(2023, 0)
        }
        assertThrows(IllegalTimeException::class.java) {
            overviewService.checkYearMonth(-1, 13)
        }
    }

    @Test
    fun checkAccess_whenUserIsAdmin_thenNoException() {
        `when`(accessService.isAdmin()).thenReturn(true)

        assertThatCode { overviewService.checkAccess(null) }
            .doesNotThrowAnyException()
    }

    @Test
    fun checkAccess_whenUserIsNotAdminAndNoAccess_thenThrowUserNotAllowedException() {
        `when`(accessService.isAdmin()).thenReturn(false)
        `when`(accessService.canReadEntries(1L)).thenReturn(false)

        assertThrows(UserNotAllowedException::class.java) {
            overviewService.checkAccess(1L)
        }
    }

    @Test
    fun checkAccess_whenUserIsNotAdminAndNoAccess_thenNoException() {
        `when`(accessService.isAdmin()).thenReturn(false)
        `when`(accessService.canReadEntries(1L)).thenReturn(true)

        assertThatCode { overviewService.checkAccess(1L) }
            .doesNotThrowAnyException()
    }

    @Test
    fun getApprovedHoursMonthly_withArchivedClient_includesArchivedClientRowAndTotals() {
        val year = 2024
        val month = 2
        val hourTypeId = 7L

        val activeClient = clientDto(1L, "Aktiv", "Alpha", archived = false)
        val archivedClient = clientDto(2L, "Archiv", "Beta", archived = true)
        val activePlan = planDto(11L, activeClient.id, year, month, hourTypeId)
        val archivedPlan = planDto(22L, archivedClient.id, year, month, hourTypeId)

        val result = overviewService.getApprovedHoursMonthly(
            listOf(activePlan, archivedPlan),
            listOf(activeClient, archivedClient),
            hourTypeId,
            year,
            month
        )

        val archivedRow = result.first { it.clientDto.id == archivedClient.id }
        val allRow = result.first { it.clientDto.id == 0L }

        assertThat(archivedRow.clientDto.archived).isTrue()
        assertThat(archivedRow.values[0]).isEqualTo(29.0)
        assertThat(allRow.values[0]).isEqualTo(58.0)
    }

    @Test
    fun getApprovedHoursYearly_withArchivedClient_includesArchivedClientRowAndTotals() {
        val year = 2024
        val hourTypeId = 7L

        val activeClient = clientDto(1L, "Aktiv", "Alpha", archived = false)
        val archivedClient = clientDto(2L, "Archiv", "Beta", archived = true)
        val activePlan = planDto(11L, activeClient.id, year, null, hourTypeId)
        val archivedPlan = planDto(22L, archivedClient.id, year, null, hourTypeId)

        val result = overviewService.getApprovedHoursYearly(
            listOf(activePlan, archivedPlan),
            listOf(activeClient, archivedClient),
            hourTypeId,
            year
        )

        val archivedRow = result.first { it.clientDto.id == archivedClient.id }
        val allRow = result.first { it.clientDto.id == 0L }

        assertThat(archivedRow.clientDto.archived).isTrue()
        assertThat(archivedRow.values[0]).isEqualTo(366.0)
        assertThat(allRow.values[0]).isEqualTo(732.0)
    }

    @Test
    fun getApprovedHoursYearly_withCorridorPlan_usesCorridorMean() {
        val year = 2024
        val hourTypeId = 7L

        val client = clientDto(1L, "Max", "Muster", archived = false)
        val corridorPlan = planDto(11L, client.id, year, null, hourTypeId, corridor = true)
        val corridor = corridorEntity(5L, hourTypeId, 300, 600)
        `when`(hourCorridorRepository.findAllById(listOf(5L))).thenReturn(listOf(corridor))

        val result = overviewService.getApprovedHoursYearly(
            listOf(corridorPlan),
            listOf(client),
            hourTypeId,
            year,
            false
        )

        val row = result.first { it.assistancePlanDto.id == corridorPlan.id }

        assertThat(row.values[2]).isCloseTo(31.0714285714, within(0.0001))
        assertThat(row.values[0]).isCloseTo(392.1428571429, within(0.0001))
    }

    @Test
    fun getDifferenceHoursYearly_withCorridorPlan_usesCorridorBounds() {
        val year = 2024
        val hourTypeId = 7L

        val client = clientDto(1L, "Max", "Muster", archived = false)
        val corridorPlan = planDto(11L, client.id, year, null, hourTypeId, corridor = true)
        val corridor = corridorEntity(5L, hourTypeId, 300, 600)
        `when`(hourCorridorRepository.findAllById(listOf(5L))).thenReturn(listOf(corridor))

        val services = listOf(service(11L, LocalDate.of(2024, 2, 10), 60))
        val result = overviewService.getDifferenceHoursYearly(
            services,
            listOf(corridorPlan),
            listOf(client),
            hourTypeId,
            year,
            false
        )

        val row = result.first { it.assistancePlanDto.id == corridorPlan.id }

        assertThat(row.values[2]).isCloseTo(-19.71, within(0.0001))
        assertThat(row.values[0]).isCloseTo(-260.41, within(0.0001))
    }

    private fun clientDto(id: Long, firstName: String, lastName: String, archived: Boolean): ClientSimpleDto {
        return ClientSimpleDto().apply {
            this.id = id
            this.firstName = firstName
            this.lastName = lastName
            this.archived = archived
        }
    }

    private fun planDto(
        id: Long,
        clientId: Long,
        year: Int,
        month: Int?,
        hourTypeId: Long,
        corridor: Boolean = false
    ): AssistancePlanDto {
        val plan = AssistancePlanDto().apply {
            this.id = id
            this.clientId = clientId
            this.start = if (month != null) LocalDate.of(year, month, 1) else LocalDate.of(year, 1, 1)
            this.end = if (month != null) LocalDate.of(year, month, 1).plusMonths(1).minusDays(1) else LocalDate.of(year, 12, 31)
            if (corridor) {
                this.hourMode = AssistancePlanHourMode.CORRIDOR
                this.hourCorridorId = 5
            }
        }

        plan.hours.add(
            AssistancePlanHourDto().apply {
                this.assistancePlanId = id
                this.hourTypeId = hourTypeId
                this.weeklyMinutes = 420
            }
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
        de.vinz.openfls.domains.services.Service(
            start = start.atTime(8, 0),
            end = start.atTime(8, 0).plusMinutes(minutes.toLong()),
            minutes = minutes,
            assistancePlan = AssistancePlan(id = assistancePlanId)
        )
}
