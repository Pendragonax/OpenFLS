package de.vinz.openfls.domains.goals.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.goals.dtos.GoalDto
import de.vinz.openfls.domains.goals.dtos.GoalHourDto
import de.vinz.openfls.domains.goals.repositories.GoalHourRepository
import de.vinz.openfls.domains.goals.repositories.GoalRepository
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.hourTypes.HourTypeRepository
import de.vinz.openfls.domains.hourTypes.HourTypeService
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean

@DataJpaTest
@Import(GoalService::class, TestBeans::class)
class GoalServiceDataJpaTest {

    @Autowired
    lateinit var goalService: GoalService

    @Autowired
    lateinit var goalRepository: GoalRepository

    @Autowired
    lateinit var goalHourRepository: GoalHourRepository

    @Autowired
    lateinit var assistancePlanRepository: AssistancePlanRepository

    @Autowired
    lateinit var hourTypeRepository: HourTypeRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @MockitoBean
    lateinit var assistancePlanService: AssistancePlanService

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var hourTypeService: HourTypeService

    @Test
    fun create_validDto_persistsGoalAndHours() {
        // Given
        val assistancePlan = assistancePlanRepository.save(AssistancePlan())
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        whenever(assistancePlanService.getById(assistancePlan.id)).thenReturn(assistancePlan)
        whenever(institutionService.getEntityById(institution.id!!)).thenReturn(institution)
        whenever(hourTypeService.getById(hourType.id)).thenReturn(hourType)

        val dto = GoalDto().apply {
            title = "Goal"
            description = "Desc"
            assistancePlanId = assistancePlan.id
            institutionId = institution.id
            hours = mutableSetOf(GoalHourDto().apply {
                weeklyHours = 5.0
                hourTypeId = hourType.id
            })
        }

        // When
        val result = goalService.create(dto)

        // Then
        val saved = goalRepository.findById(result.id)
        assertThat(saved).isPresent
        val hours = goalHourRepository.findByGoalId(result.id)
        assertThat(hours).hasSize(1)
    }

    @Test
    fun create_missingAssistancePlan_throwsException() {
        // Given
        whenever(assistancePlanService.getById(9999)).thenReturn(null)
        val dto = GoalDto().apply {
            title = "Goal"
            description = "Desc"
            assistancePlanId = 9999
        }

        // When / Then
        assertThatThrownBy { goalService.create(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_missingGoal_throwsException() {
        // Given
        val assistancePlan = assistancePlanRepository.save(AssistancePlan())
        whenever(assistancePlanService.getById(assistancePlan.id)).thenReturn(assistancePlan)
        val dto = GoalDto().apply {
            id = 9999
            title = "Goal"
            description = "Desc"
            assistancePlanId = assistancePlan.id
        }

        // When / Then
        assertThatThrownBy { goalService.update(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_existingGoal_updatesHours() {
        // Given
        val assistancePlan = assistancePlanRepository.save(AssistancePlan())
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))
        whenever(assistancePlanService.getById(assistancePlan.id)).thenReturn(assistancePlan)
        whenever(hourTypeService.getById(hourType.id)).thenReturn(hourType)

        val existing = goalRepository.save(de.vinz.openfls.domains.goals.entities.Goal(
            title = "Old",
            description = "Old",
            assistancePlan = assistancePlan
        ))
        goalHourRepository.save(de.vinz.openfls.domains.goals.entities.GoalHour(weeklyHours = 1.0, goal = existing, hourType = hourType))

        val dto = GoalDto().apply {
            id = existing.id
            title = "New"
            description = "New"
            assistancePlanId = assistancePlan.id
            hours = mutableSetOf(GoalHourDto().apply {
                weeklyHours = 3.0
                hourTypeId = hourType.id
            })
        }

        // When
        val result = goalService.update(dto)

        // Then
        val saved = goalRepository.findById(result.id)
        assertThat(saved).isPresent
        val hours = goalHourRepository.findByGoalId(result.id)
        assertThat(hours).hasSize(1)
        assertThat(hours.first().weeklyHours).isEqualTo(3.0)
    }
}
