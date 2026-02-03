package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourDto
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanHourRepository
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.hourTypes.HourTypeRepository
import de.vinz.openfls.domains.hourTypes.HourTypeService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean

@DataJpaTest
@Import(AssistancePlanHourService::class, TestBeans::class)
class AssistancePlanHourServiceDataJpaTest {

    @Autowired
    lateinit var assistancePlanHourService: AssistancePlanHourService

    @Autowired
    lateinit var assistancePlanHourRepository: AssistancePlanHourRepository

    @Autowired
    lateinit var assistancePlanRepository: AssistancePlanRepository

    @Autowired
    lateinit var hourTypeRepository: HourTypeRepository

    @MockitoBean
    lateinit var assistancePlanService: AssistancePlanService

    @MockitoBean
    lateinit var hourTypeService: HourTypeService

    @Test
    fun save_validDto_persistsEntity() {
        // Given
        val assistancePlan = assistancePlanRepository.save(AssistancePlan())
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))
        whenever(assistancePlanService.getById(assistancePlan.id)).thenReturn(assistancePlan)
        whenever(hourTypeService.getById(hourType.id)).thenReturn(hourType)

        val dto = AssistancePlanHourDto().apply {
            weeklyHours = 8.0
            assistancePlanId = assistancePlan.id
            hourTypeId = hourType.id
        }

        // When
        val result = assistancePlanHourService.save(dto)

        // Then
        val saved = assistancePlanHourRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().weeklyHours).isEqualTo(8.0)
    }
}
