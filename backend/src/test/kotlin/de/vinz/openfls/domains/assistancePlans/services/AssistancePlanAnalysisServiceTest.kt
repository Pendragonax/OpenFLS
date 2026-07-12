package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanAnalysisMonthCollectionDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanAnalysisMonthDto
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanProjection
import de.vinz.openfls.domains.clients.projections.ClientSoloProjection
import de.vinz.openfls.domains.goals.projections.GoalProjection
import de.vinz.openfls.domains.hourCorridors.HourCorridor
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.institutions.projections.InstitutionSoloProjection
import de.vinz.openfls.domains.services.projections.ServiceSoloProjection
import de.vinz.openfls.domains.services.services.ServiceService
import de.vinz.openfls.domains.sponsors.projections.SponsorSoloProjection
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

class AssistancePlanAnalysisServiceTest {

    private val assistancePlanService: AssistancePlanService = mock()
    private val serviceService: ServiceService = mock()
    private val accessService: de.vinz.openfls.domains.permissions.AccessService = mock()
    private val analysisService = AssistancePlanAnalysisService(
        assistancePlanService,
        serviceService,
        accessService
    )

    @Test
    fun getAnalysisInMonth_corridorPlan_marksKorAndUsesAverageApprovedHours() {
        // Given
        val today = LocalDate.now()
        val planStart = today.withDayOfMonth(1)
        val planEnd = today.withDayOfMonth(today.lengthOfMonth())
        val monthDays = DateService.countDaysOfMonthAndYearBetweenStartAndEnd(
            today.year,
            today.monthValue,
            planStart,
            planEnd
        )
        val hourType = HourType(id = 1, title = "Korridor")
        val projection = corridorProjection(planStart, planEnd, hourType)
        val executedMinutes = 3000

        whenever(
            serviceService.getAllByAssistancePlanIdAndYearAndMonth(
                assistancePlanId = projection.id,
                year = today.year,
                month = today.monthValue
            )
        ).thenReturn(listOf(serviceProjection(projection.id, executedMinutes)))

        // When
        val result = analysisService.getAnalysisInMonth(today.year, today.monthValue, listOf(projection))

        // Then
        assertThat(result).hasSize(1)
        val entry = result.first()
        val expectedApprovedHours = TimeDoubleService.convertDoubleToTimeDouble((monthDays * 7.5) / 7.0)
        val expectedMonthlyTo = TimeDoubleService.convertDoubleToTimeDouble((monthDays * 10.0) / 7.0)

        assertThat(result.first().approvedHours).isEqualTo(expectedApprovedHours)
        assertThat(entry.clientLastName).isEqualTo("Muster [Kor]")
        assertThat(entry.missingHours).isNegative()
        assertThat(entry.executedPercent).isEqualTo(
            TimeDoubleService.roundDoubleToTwoDigits((executedMinutes / 60.0) * 100 / expectedMonthlyTo)
        )
    }

    @Test
    fun getAnalysisByHourTypeIdInMonth_corridorPlan_usesMonthlyUpperBoundForUtilization() {
        // Given
        val today = LocalDate.now()
        val planStart = today.withDayOfMonth(1)
        val planEnd = today.withDayOfMonth(today.lengthOfMonth())
        val hourType = HourType(id = 2, title = "Korridor")
        val projection = corridorProjection(planStart, planEnd, hourType)
        val executedMinutes = 3000

        whenever(
            serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(
                assistancePlanId = projection.id,
                hourTypeId = hourType.id,
                year = today.year,
                month = today.monthValue
            )
        ).thenReturn(listOf(serviceProjection(projection.id, executedMinutes)))

        // When
        val result = analysisService.getAnalysisByHourTypeIdInMonth(today.year, today.monthValue, projection, hourType.id)

        // Then
        val monthDays = DateService.countDaysOfMonthAndYearBetweenStartAndEnd(
            today.year,
            today.monthValue,
            planStart,
            planEnd
        )
        val expectedApprovedHours = TimeDoubleService.convertDoubleToTimeDouble((monthDays * 7.5) / 7.0)
        val expectedMonthlyFrom = TimeDoubleService.convertDoubleToTimeDouble((monthDays * 5.0) / 7.0)
        val expectedMonthlyTo = TimeDoubleService.convertDoubleToTimeDouble((monthDays * 10.0) / 7.0)

        assertThat(result.approvedHours).isEqualTo(expectedApprovedHours)
        assertThat(result.clientLastName).isEqualTo("Muster [Kor]")
        assertThat(result.missingHours).isEqualTo(
            TimeDoubleService.diffTimeDoubles(expectedMonthlyTo, TimeDoubleService.convertDoubleToTimeDouble(executedMinutes / 60.0))
        )
        assertThat(result.executedPercent).isEqualTo(
            TimeDoubleService.roundDoubleToTwoDigits((executedMinutes / 60.0) * 100 / expectedMonthlyTo)
        )
    }

    private fun corridorProjection(start: LocalDate, end: LocalDate, hourType: HourType): AssistancePlanProjection {
        val corridor = HourCorridor(
            id = 5,
            title = "5 bis 10",
            weeklyMinutesFrom = 300,
            weeklyMinutesTill = 600,
            hourType = hourType
        )

        return object : AssistancePlanProjection {
            override val id: Long = 5
            override val start: LocalDate = start
            override val end: LocalDate = end
            override val client: ClientSoloProjection = clientProjection()
            override val sponsor: SponsorSoloProjection = sponsorProjection()
            override val institution: InstitutionSoloProjection = institutionProjection()
            override val hourMode: AssistancePlanHourMode = AssistancePlanHourMode.CORRIDOR
            override val hourCorridor: HourCorridor? = corridor
            override val hours: List<de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanHourProjection> = emptyList()
            override val goals: List<GoalProjection> = emptyList()
        }
    }

    private fun clientProjection(): ClientSoloProjection {
        return object : ClientSoloProjection {
            override val id: Long = 1
            override val firstName: String = "Max"
            override val lastName: String = "Muster"
            override val phoneNumber: String = ""
            override val email: String = ""
            override val archived: Boolean = false
        }
    }

    private fun sponsorProjection(): SponsorSoloProjection {
        return object : SponsorSoloProjection {
            override val id: Long = 1
            override val name: String = "Sponsor"
            override val payOverhang: Boolean = true
            override val payExact: Boolean = false
        }
    }

    private fun institutionProjection(): InstitutionSoloProjection {
        return object : InstitutionSoloProjection {
            override val id: Long = 1
            override val name: String = "Institution"
            override val email: String = ""
            override val phonenumber: String = ""
        }
    }

    private fun serviceProjection(assistancePlanId: Long, minutes: Int): ServiceSoloProjection {
        return object : ServiceSoloProjection {
            override val id: Long = 1
            override val start: LocalDateTime = LocalDate.now().atStartOfDay()
            override val end: LocalDateTime = start.plusMinutes(minutes.toLong())
            override val minutes: Int = minutes
            override val title: String = ""
            override val content: String = ""
            override val unfinished: Boolean = false
            override val groupService: Boolean = false
        }
    }
}
