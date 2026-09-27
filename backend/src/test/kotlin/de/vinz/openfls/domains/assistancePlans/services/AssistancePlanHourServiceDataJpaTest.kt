package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourDto
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanHourRepository
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.hourTypes.repository.HourTypeRepository
import de.vinz.openfls.domains.hourTypes.service.HourTypeService
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
        whenever(assistancePlanService.getEntityById(assistancePlan.id)).thenReturn(assistancePlan)
        whenever(hourTypeService.getEntityById(hourType.id)).thenReturn(hourType)

        val dto = AssistancePlanHourDto().apply {
            weeklyMinutes = 480
            assistancePlanId = assistancePlan.id
            hourTypeId = hourType.id
        }

        // When
        val result = assistancePlanHourService.save(dto)

        // Then
        val saved = assistancePlanHourRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().weeklyMinutes).isEqualTo(480)
        assertThat(result.weeklyMinutes).isEqualTo(480)
        assertThat(result.assistancePlanId).isEqualTo(assistancePlan.id)
        assertThat(result.hourTypeId).isEqualTo(hourType.id)
        assertThat(result.hourTypeTitle).isEqualTo("Standard")
    }

    @Test
    fun getById_existingEntity_returnsResponseDto() {
        // Given
        val assistancePlan = assistancePlanRepository.save(AssistancePlan())
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))
        val saved = assistancePlanHourRepository.save(
            de.vinz.openfls.domains.assistancePlans.AssistancePlanHour(
                weeklyMinutes = 90,
                hourType = hourType,
                assistancePlan = assistancePlan
            )
        )

        // When
        val result = assistancePlanHourService.getById(saved.id)

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(saved.id)
        assertThat(result.weeklyMinutes).isEqualTo(90)
        assertThat(result.assistancePlanId).isEqualTo(assistancePlan.id)
        assertThat(result.hourTypeId).isEqualTo(hourType.id)
        assertThat(result.hourTypeTitle).isEqualTo("Standard")
    }

    @Test
    fun getById_missingEntity_returnsNull() {
        // When
        val result = assistancePlanHourService.getById(999999L)

        // Then
        assertThat(result).isNull()
    }
}
