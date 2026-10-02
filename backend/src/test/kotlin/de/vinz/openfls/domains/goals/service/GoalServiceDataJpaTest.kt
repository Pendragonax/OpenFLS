package de.vinz.openfls.domains.goals.service

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.goals.dto.GoalCreateRequest
import de.vinz.openfls.domains.goals.dto.GoalCreateResult
import de.vinz.openfls.domains.goals.dto.GoalDeleteResult
import de.vinz.openfls.domains.goals.dto.GoalHourRequest
import de.vinz.openfls.domains.goals.dto.GoalUpdateRequest
import de.vinz.openfls.domains.goals.dto.GoalUpdateResult
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.goals.entity.GoalHour
import de.vinz.openfls.domains.goals.repository.GoalHourRepository
import de.vinz.openfls.domains.goals.repository.GoalRepository
import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor
import de.vinz.openfls.domains.hourCorridors.repository.HourCorridorRepository
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.hourTypes.repository.HourTypeRepository
import de.vinz.openfls.domains.hourTypes.service.HourTypeService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
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

    @Autowired
    lateinit var hourCorridorRepository: HourCorridorRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @MockitoBean
    lateinit var assistancePlanService: AssistancePlanService

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var hourTypeService: HourTypeService

    @Test
    fun create_validRequest_persistsGoalAndHours() {
        // Given
        val assistancePlan = assistancePlanRepository.save(AssistancePlan())
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        whenever(assistancePlanService.getEntityById(assistancePlan.id)).thenReturn(assistancePlan)
        whenever(institutionService.getEntityById(institution.id!!)).thenReturn(institution)
        whenever(hourTypeService.getEntityById(hourType.id)).thenReturn(hourType)

        val request = GoalCreateRequest(
            title = "Goal",
            description = "Desc",
            assistancePlanId = assistancePlan.id,
            institutionId = institution.id,
            hours = listOf(GoalHourRequest(weeklyMinutes = 300, hourTypeId = hourType.id))
        )

        // When
        val result = goalService.create(request) as GoalCreateResult.Success

        // Then
        val saved = goalRepository.findById(result.response.id)
        assertThat(saved).isPresent
        val hours = goalHourRepository.findByGoalId(result.response.id)
        assertThat(hours).hasSize(1)
        assertThat(result.response.institutionName).isEqualTo("Inst")
        assertThat(result.response.hours.first().hourTypeTitle).isEqualTo("Standard")
    }

    @Test
    fun create_missingAssistancePlan_returnsAssistancePlanNotFound() {
        // Given
        whenever(assistancePlanService.getEntityById(9999)).thenReturn(null)
        val request = GoalCreateRequest(
            title = "Goal",
            description = "Desc",
            assistancePlanId = 9999
        )

        // When
        val result = goalService.create(request)

        // Then
        assertThat(result).isEqualTo(
            GoalCreateResult.AssistancePlanNotFound("assistance plan [id = 9999] not found")
        )
    }

    @Test
    fun update_missingGoal_returnsNotFound() {
        // Given
        val request = GoalUpdateRequest(
            id = 9999,
            title = "Goal",
            description = "Desc",
            assistancePlanId = 1
        )

        // When
        val result = goalService.update(request)

        // Then
        assertThat(result).isEqualTo(GoalUpdateResult.NotFound)
    }

    @Test
    fun update_existingGoal_updatesHours() {
        // Given
        val assistancePlan = assistancePlanRepository.save(AssistancePlan())
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))
        whenever(assistancePlanService.getEntityById(assistancePlan.id)).thenReturn(assistancePlan)
        whenever(hourTypeService.getEntityById(hourType.id)).thenReturn(hourType)

        val existing = goalRepository.save(
            Goal(title = "Old", description = "Old", assistancePlan = assistancePlan)
        )
        goalHourRepository.save(GoalHour(weeklyMinutes = 60, goal = existing, hourType = hourType))

        val request = GoalUpdateRequest(
            id = existing.id,
            title = "New",
            description = "New",
            assistancePlanId = assistancePlan.id,
            hours = listOf(GoalHourRequest(weeklyMinutes = 180, hourTypeId = hourType.id))
        )

        // When
        val result = goalService.update(request) as GoalUpdateResult.Success

        // Then
        val saved = goalRepository.findById(result.response.id)
        assertThat(saved).isPresent
        val hours = goalHourRepository.findByGoalId(result.response.id)
        assertThat(hours).hasSize(1)
        assertThat(hours.first().weeklyMinutes).isEqualTo(180)
    }

    @Test
    fun update_hourFromAnotherGoal_returnsHourNotInGoal() {
        // Given
        val assistancePlan = assistancePlanRepository.save(AssistancePlan())
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))
        whenever(assistancePlanService.getEntityById(assistancePlan.id)).thenReturn(assistancePlan)
        whenever(hourTypeService.getEntityById(hourType.id)).thenReturn(hourType)

        val goal = goalRepository.save(Goal(title = "Goal", description = "Desc", assistancePlan = assistancePlan))
        val otherGoal = goalRepository.save(Goal(title = "Other", description = "Desc", assistancePlan = assistancePlan))
        val foreignHour = goalHourRepository.save(GoalHour(weeklyMinutes = 60, goal = otherGoal, hourType = hourType))

        val request = GoalUpdateRequest(
            id = goal.id,
            title = "Goal",
            description = "Desc",
            assistancePlanId = assistancePlan.id,
            hours = listOf(GoalHourRequest(id = foreignHour.id, weeklyMinutes = 60, hourTypeId = hourType.id))
        )

        // When
        val result = goalService.update(request)

        // Then
        assertThat(result).isEqualTo(
            GoalUpdateResult.HourNotInGoal("goal hour with id ${foreignHour.id} does not belong to goal ${goal.id}")
        )
        assertThat(goalHourRepository.findById(foreignHour.id).get().goal?.id).isEqualTo(otherGoal.id)
    }

    @Test
    fun create_corridorAssistancePlanWithGoalHours_returnsCorridorHoursNotAllowed() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))
        val corridor = hourCorridorRepository.save(
            HourCorridor(
                title = "5 bis 10",
                weeklyMinutesFrom = 300,
                weeklyMinutesTill = 600,
                hourType = hourType
            )
        )
        val assistancePlan = assistancePlanRepository.save(
            AssistancePlan(hourMode = AssistancePlanHourMode.CORRIDOR, hourCorridor = corridor)
        )
        whenever(assistancePlanService.getEntityById(assistancePlan.id)).thenReturn(assistancePlan)
        whenever(hourTypeService.getEntityById(hourType.id)).thenReturn(hourType)

        val request = GoalCreateRequest(
            title = "Goal",
            description = "Desc",
            assistancePlanId = assistancePlan.id,
            hours = listOf(GoalHourRequest(weeklyMinutes = 300, hourTypeId = hourType.id))
        )

        // When
        val result = goalService.create(request)

        // Then
        assertThat(result).isEqualTo(
            GoalCreateResult.CorridorHoursNotAllowed("corridor assistance plans must not contain goal hours")
        )
    }

    @Test
    fun delete_existingGoal_removesGoalAndHours() {
        // Given
        val assistancePlan = assistancePlanRepository.save(AssistancePlan())
        val goal = goalRepository.save(Goal(title = "Goal", description = "Desc", assistancePlan = assistancePlan))

        // When
        val result = goalService.delete(goal.id) as GoalDeleteResult.Success

        // Then
        assertThat(result.response.id).isEqualTo(goal.id)
        assertThat(goalRepository.findById(goal.id)).isEmpty
    }

    @Test
    fun delete_missingGoal_returnsNotFound() {
        // When
        val result = goalService.delete(9999)

        // Then
        assertThat(result).isEqualTo(GoalDeleteResult.NotFound)
    }

    @Test
    fun getByAssistancePlanId_returnsGoalsWithHours() {
        // Given
        val assistancePlan = assistancePlanRepository.save(AssistancePlan())
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))
        val goal = goalRepository.save(Goal(title = "Goal", description = "Desc", assistancePlan = assistancePlan))
        goalHourRepository.save(GoalHour(weeklyMinutes = 60, goal = goal, hourType = hourType))
        entityManager.flush()
        entityManager.clear()

        // When
        val result = goalService.getByAssistancePlanId(assistancePlan.id)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result.first().hours).hasSize(1)
        assertThat(result.first().hours.first().hourTypeTitle).isEqualTo("Standard")
    }

    @Test
    fun getEntityById_existingGoal_returnsEntity() {
        // Given
        val goal = goalRepository.save(Goal(title = "Goal"))

        // When
        val result = goalService.getEntityById(goal.id)

        // Then
        assertThat(result?.id).isEqualTo(goal.id)
    }

    @Test
    fun getEntityById_missingGoal_returnsNull() {
        // When
        val result = goalService.getEntityById(9999)

        // Then
        assertThat(result).isNull()
    }
}
