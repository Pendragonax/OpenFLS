package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanEvalDto
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.services.Service
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.modelmapper.ModelMapper
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

class AssistancePlanEvaluationServiceTest {

    private val assistancePlanService: AssistancePlanService = mock()
    private val serviceService: de.vinz.openfls.domains.services.services.ServiceService = mock()
    private val evaluationService = AssistancePlanEvaluationService(
        assistancePlanService,
        serviceService,
        ModelMapper()
    )

    @Test
    fun getEvaluationById_withMatchingServices_returnsCalculatedValues() {
        // Given
        val hourType = HourType(id = 1, title = "Standard")
        val plan = AssistancePlan(
            id = 11,
            start = LocalDate.of(2026, 5, 1),
            end = LocalDate.of(2026, 5, 7)
        )
        plan.hours = mutableSetOf(
            AssistancePlanHour(
                weeklyMinutes = 60,
                hourType = hourType,
                assistancePlan = plan
            )
        )
        whenever(assistancePlanService.getById(plan.id)).thenReturn(plan)
        whenever(serviceService.getByAssistancePlan(plan.id)).thenReturn(
            listOf(
                Service(
                    id = 21,
                    start = LocalDateTime.of(2026, 5, 3, 8, 0),
                    end = LocalDateTime.of(2026, 5, 3, 9, 0),
                    minutes = 60,
                    hourType = hourType,
                    assistancePlan = plan
                )
            )
        )

        // When
        val result = evaluationService.getEvaluationById(plan.id)

        // Then
        assertThat(result.total).hasSize(1)
        assertThat(result.total.first().target).isEqualTo(1.0)
        assertThat(result.total.first().actual).isEqualTo(1.0)
        assertThat(result.total.first().size).isEqualTo(1)
        assertThat(result.tillToday.first().actual).isEqualTo(1.0)
        assertThat(result.actualMonth.first().target).isEqualTo(1.0)
        assertThat(result.actualYear.first().target).isEqualTo(1.0)
        assertThat(result.notMatchingServices).isZero()
        assertThat(result.notMatchingServicesIds).isEmpty()
    }

    @Test
    fun getEvaluationById_missingPlan_throwsException() {
        // Given
        whenever(assistancePlanService.getById(99L)).thenReturn(null)

        // When / Then
        assertThatThrownBy { evaluationService.getEvaluationById(99L) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("id not found ")
    }
}
