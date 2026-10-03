package de.vinz.openfls.domains.assistancePlans.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanCreateGoalRequest
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanCreateHourRequest
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanCreateRequest
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanCreateResult
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanDeleteResult
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanUpdateGoalHourRequest
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanUpdateGoalRequest
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanUpdateHourRequest
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanUpdateRequest
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanUpdateResult
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.repository.AssistancePlanRepository
import de.vinz.openfls.domains.categories.entity.CategoryTemplate
import de.vinz.openfls.domains.categories.repository.CategoryTemplateRepository
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.clients.repository.ClientRepository
import de.vinz.openfls.domains.clients.service.ClientService
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.goals.entity.GoalHour
import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor
import de.vinz.openfls.domains.hourCorridors.repository.HourCorridorRepository
import de.vinz.openfls.domains.hourCorridors.service.HourCorridorService
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.hourTypes.repository.HourTypeRepository
import de.vinz.openfls.domains.hourTypes.service.HourTypeService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.domains.sponsors.entity.Sponsor
import de.vinz.openfls.domains.sponsors.repository.SponsorRepository
import de.vinz.openfls.domains.sponsors.service.SponsorService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate

@DataJpaTest
@Import(AssistancePlanService::class, TestBeans::class)
class AssistancePlanServiceDataJpaTest {

    @Autowired
    lateinit var assistancePlanService: AssistancePlanService

    @Autowired
    lateinit var assistancePlanRepository: AssistancePlanRepository

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var categoryTemplateRepository: CategoryTemplateRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Autowired
    lateinit var sponsorRepository: SponsorRepository

    @Autowired
    lateinit var hourTypeRepository: HourTypeRepository

    @Autowired
    lateinit var hourCorridorRepository: HourCorridorRepository

    @MockitoBean
    lateinit var clientService: ClientService

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var sponsorService: SponsorService

    @MockitoBean
    lateinit var hourTypeService: HourTypeService

    @MockitoBean
    lateinit var hourCorridorService: HourCorridorService

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    private lateinit var institution: Institution
    private lateinit var client: Client
    private lateinit var sponsor: Sponsor
    private lateinit var hourType: HourType

    @BeforeEach
    fun setUp() {
        institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        client = clientRepository.save(
            Client(firstName = "Max", lastName = "Mustermann", categoryTemplate = categoryTemplate(), institution = institution)
        )
        sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))
        hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))

        whenever(clientService.getEntityById(client.id)).thenReturn(client)
        whenever(institutionService.getEntityById(institution.id!!)).thenReturn(institution)
        whenever(sponsorService.getEntityById(sponsor.id)).thenReturn(sponsor)
        whenever(hourTypeService.getEntityById(hourType.id)).thenReturn(hourType)
    }

    @Test
    fun getDetailById_corridorPlan_returnsFullyPopulatedDetachedResponse() {
        // Given
        val corridor = hourCorridorRepository.save(
            HourCorridor(title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourType = hourType)
        )
        val plan = assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = institution,
                hourMode = AssistancePlanHourMode.CORRIDOR,
                hourCorridor = corridor
            )
        )
        val goal = Goal(title = "Goal 1", description = "Description", institution = institution, assistancePlan = plan)
        goal.hours.add(GoalHour(weeklyMinutes = 45, hourType = hourType, goal = goal))
        plan.goals.add(goal)
        plan.hours.add(AssistancePlanHour(weeklyMinutes = 120, hourType = hourType, assistancePlan = plan))
        assistancePlanRepository.save(plan)

        // simulate a fresh request: nothing pre-loaded in the persistence context
        testEntityManager.flush()
        testEntityManager.clear()

        // When
        val result = assistancePlanService.getDetailById(plan.id)

        // detach everything -> mirrors serialization after the transaction/session has closed
        testEntityManager.clear()

        // Then
        val response = result!!
        assertThat(response.client.firstName).isEqualTo("Max")
        assertThat(response.client.lastName).isEqualTo("Mustermann")
        assertThat(response.sponsor.name).isEqualTo("Sponsor")
        assertThat(response.institution.name).isEqualTo("Inst")
        assertThat(response.hourMode).isEqualTo(AssistancePlanHourMode.CORRIDOR)
        val corridorResponse = response.hourCorridor!!
        assertThat(corridorResponse.title).isEqualTo("5 bis 10")
        assertThat(corridorResponse.hourTypeId).isEqualTo(hourType.id)
        assertThat(corridorResponse.hourTypeTitle).isEqualTo("Standard")
        assertThat(response.hours).hasSize(1)
        assertThat(response.hours.first().hourType.title).isEqualTo("Standard")
        assertThat(response.goals).hasSize(1)
        assertThat(response.goals.first().hours).hasSize(1)
        assertThat(response.goals.first().hours.first().hourType.title).isEqualTo("Standard")

        // the response must be serializable without any Hibernate session (no leaked lazy proxies)
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        assertThatCode { objectMapper.writeValueAsString(response) }.doesNotThrowAnyException()
    }

    @Test
    fun getDetailById_exactPlanWithoutCorridor_returnsResponseWithNullCorridor() {
        // Given
        val plan = savePlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
        testEntityManager.flush()
        testEntityManager.clear()

        // When
        val result = assistancePlanService.getDetailById(plan.id)
        testEntityManager.clear()

        // Then
        assertThat(result!!.hourCorridor).isNull()
        assertThat(result.client.firstName).isEqualTo("Max")
        assertThat(result.goals).isEmpty()
        assertThat(result.hours).isEmpty()
        assertThatCode { jacksonObjectMapper().findAndRegisterModules().writeValueAsString(result) }
            .doesNotThrowAnyException()
    }

    @Test
    fun getDetailById_unknownId_returnsNull() {
        assertThat(assistancePlanService.getDetailById(9_999_999L)).isNull()
    }

    @Test
    fun create_goalHoursOnly_persistsGoalsAndGoalHours() {
        // Given
        val request = createRequest().apply {
            goals = listOf(createGoal("Goal 1", listOf(createHour(60))))
        }

        // When
        val result = assistancePlanService.create(request)

        // Then
        val response = (result as AssistancePlanCreateResult.Success).response
        assertThat(response.institutionName).isEqualTo("Inst")
        val saved = assistancePlanRepository.findById(response.id).get()
        assertThat(saved.hours).isEmpty()
        assertThat(saved.goals).hasSize(1)
        assertThat(saved.goals.first().hours).hasSize(1)
    }

    @Test
    fun create_severalNewGoalsAndHours_persistsAllOfThem() {
        // Given
        val request = createRequest().apply {
            hours = listOf(createHour(60), createHour(30))
            goals = listOf(createGoal("Goal 1"), createGoal("Goal 2"), createGoal("Goal 3"))
        }

        // When
        val result = assistancePlanService.create(request)

        // Then
        val id = (result as AssistancePlanCreateResult.Success).response.id
        testEntityManager.flush()
        testEntityManager.clear()
        val saved = assistancePlanRepository.findById(id).get()
        assertThat(saved.goals.map { it.title }).containsExactlyInAnyOrder("Goal 1", "Goal 2", "Goal 3")
        assertThat(saved.hours).hasSize(2)
    }

    @Test
    fun create_planHoursAndGoalHours_returnsInvalidHoursWithGermanMessage() {
        // Given
        val request = createRequest().apply {
            hours = listOf(createHour(120))
            goals = listOf(createGoal("Goal 1", listOf(createHour(60))))
        }

        // When
        val result = assistancePlanService.create(request)

        // Then
        assertThat(result).isEqualTo(
            AssistancePlanCreateResult.InvalidHours(
                "Stunden dürfen entweder direkt im Hilfeplan oder in den Zielen hinterlegt sein, nicht in beiden Bereichen gleichzeitig."
            )
        )
        assertThat(assistancePlanRepository.count()).isZero()
    }

    @Test
    fun create_corridorMode_persistsHourCorridor() {
        // Given
        val corridor = hourCorridorRepository.save(
            HourCorridor(title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourType = hourType)
        )
        whenever(hourCorridorService.getEntityById(corridor.id)).thenReturn(corridor)
        val request = createRequest().apply {
            hourMode = AssistancePlanHourMode.CORRIDOR
            hourCorridorId = corridor.id
        }

        // When
        val result = assistancePlanService.create(request)

        // Then
        val response = (result as AssistancePlanCreateResult.Success).response
        assertThat(response.hourMode).isEqualTo(AssistancePlanHourMode.CORRIDOR)
        assertThat(response.hourCorridorId).isEqualTo(corridor.id)
        assertThat(assistancePlanRepository.findById(response.id).get().hourCorridor).isEqualTo(corridor)
    }

    @Test
    fun create_corridorModeWithPlanOrGoalHours_returnsInvalidHours() {
        // Given
        val withPlanHours = createRequest().apply {
            hourMode = AssistancePlanHourMode.CORRIDOR
            hourCorridorId = 1
            hours = listOf(createHour(60))
        }
        val withGoalHours = createRequest().apply {
            hourMode = AssistancePlanHourMode.CORRIDOR
            hourCorridorId = 1
            goals = listOf(createGoal("Goal 1", listOf(createHour(60))))
        }
        val withoutCorridor = createRequest().apply { hourMode = AssistancePlanHourMode.CORRIDOR }

        // When / Then
        assertThat(assistancePlanService.create(withPlanHours))
            .isEqualTo(AssistancePlanCreateResult.InvalidHours("corridor assistance plans must not contain plan hours"))
        assertThat(assistancePlanService.create(withGoalHours))
            .isEqualTo(AssistancePlanCreateResult.InvalidHours("corridor assistance plans must not contain goal hours"))
        assertThat(assistancePlanService.create(withoutCorridor))
            .isEqualTo(AssistancePlanCreateResult.InvalidHours("corridor assistance plans require an hour corridor"))
    }

    @Test
    fun create_exactModeWithCorridor_returnsInvalidHours() {
        // Given
        val request = createRequest().apply { hourCorridorId = 3 }

        // When / Then
        assertThat(assistancePlanService.create(request))
            .isEqualTo(AssistancePlanCreateResult.InvalidHours("exact assistance plans must not reference an hour corridor"))
    }

    @Test
    fun create_unknownReferences_returnMatchingResultsAndPersistNothing() {
        // When / Then
        assertThat(assistancePlanService.create(createRequest().apply { clientId = 9999 }))
            .isEqualTo(AssistancePlanCreateResult.ClientNotFound)
        assertThat(assistancePlanService.create(createRequest().apply { institutionId = 9999 }))
            .isEqualTo(AssistancePlanCreateResult.InstitutionNotFound)
        assertThat(assistancePlanService.create(createRequest().apply { sponsorId = 9999 }))
            .isEqualTo(AssistancePlanCreateResult.SponsorNotFound)
        assertThat(assistancePlanService.create(createRequest().apply { hours = listOf(createHour(60, hourTypeId = 9999)) }))
            .isEqualTo(AssistancePlanCreateResult.HourTypeNotFound)
        assertThat(assistancePlanService.create(createRequest().apply {
            goals = listOf(createGoal("Goal 1").apply { institutionId = 9999 })
        })).isEqualTo(AssistancePlanCreateResult.InstitutionNotFound)
        assertThat(assistancePlanService.create(createRequest().apply {
            hourMode = AssistancePlanHourMode.CORRIDOR
            hourCorridorId = 9999
        })).isEqualTo(AssistancePlanCreateResult.HourCorridorNotFound)
        assertThat(assistancePlanRepository.count()).isZero()
    }

    @Test
    fun create_archivedClient_returnsClientArchived() {
        // Given
        val archivedClient = clientRepository.save(
            Client(firstName = "Erika", lastName = "Beispiel", categoryTemplate = categoryTemplate(), institution = institution, archived = true)
        )
        whenever(clientService.getEntityById(archivedClient.id)).thenReturn(archivedClient)

        // When
        val result = assistancePlanService.create(createRequest().apply { clientId = archivedClient.id })

        // Then
        assertThat(result).isEqualTo(AssistancePlanCreateResult.ClientArchived)
    }

    @Test
    fun update_goalWithGoalHours_updatesGoalAndGoalHourInPlace() {
        // Given
        val created = (assistancePlanService.create(createRequest().apply {
            goals = listOf(createGoal("Goal Old", listOf(createHour(60))))
        }) as AssistancePlanCreateResult.Success).response
        val existingGoal = assistancePlanRepository.findById(created.id).get().goals.first()
        val existingGoalHour = existingGoal.hours.first()

        val request = updateRequest(created.id).apply {
            start = LocalDate.of(2026, 2, 1)
            end = LocalDate.of(2026, 11, 30)
            goals = listOf(
                updateGoal(existingGoal.id, "Goal New", listOf(updateGoalHour(existingGoalHour.id, 90)))
            )
        }

        // When
        val result = assistancePlanService.update(created.id, request)

        // Then
        val response = (result as AssistancePlanUpdateResult.Success).response
        testEntityManager.flush()
        testEntityManager.clear()
        val saved = assistancePlanRepository.findById(response.id).get()
        assertThat(saved.start).isEqualTo(LocalDate.of(2026, 2, 1))
        assertThat(saved.end).isEqualTo(LocalDate.of(2026, 11, 30))
        assertThat(saved.goals).hasSize(1)
        assertThat(saved.goals.first().id).isEqualTo(existingGoal.id)
        assertThat(saved.goals.first().title).isEqualTo("Goal New")
        assertThat(saved.goals.first().hours).hasSize(1)
        assertThat(saved.goals.first().hours.first().id).isEqualTo(existingGoalHour.id)
        assertThat(saved.goals.first().hours.first().weeklyMinutes).isEqualTo(90)
    }

    @Test
    fun update_missingGoalsAndHours_areDeletedAndNewOnesAreAdded() {
        // Given
        val created = (assistancePlanService.create(createRequest().apply {
            hours = listOf(createHour(60), createHour(30))
            goals = listOf(createGoal("Goal Keep"), createGoal("Goal Drop"))
        }) as AssistancePlanCreateResult.Success).response
        val existing = assistancePlanRepository.findById(created.id).get()
        val keptHour = existing.hours.first { it.weeklyMinutes == 60 }
        val keptGoal = existing.goals.first { it.title == "Goal Keep" }

        val request = updateRequest(created.id).apply {
            hours = listOf(updateHour(keptHour.id, 75), updateHour(0, 15))
            goals = listOf(updateGoal(keptGoal.id, "Goal Keep"), updateGoal(0, "Goal New"))
        }

        // When
        val result = assistancePlanService.update(created.id, request)

        // Then
        assertThat(result).isInstanceOf(AssistancePlanUpdateResult.Success::class.java)
        testEntityManager.flush()
        testEntityManager.clear()
        val saved = assistancePlanRepository.findById(created.id).get()
        assertThat(saved.hours.map { it.weeklyMinutes }).containsExactlyInAnyOrder(75, 15)
        assertThat(saved.hours.first { it.weeklyMinutes == 75 }.id).isEqualTo(keptHour.id)
        assertThat(saved.goals.map { it.title }).containsExactlyInAnyOrder("Goal Keep", "Goal New")
        assertThat(saved.goals.first { it.title == "Goal Keep" }.id).isEqualTo(keptGoal.id)
    }

    @Test
    fun update_hourIdOfAnotherPlan_isTreatedAsNewHour() {
        // Given
        val other = savePlan(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)).also {
            it.hours.add(AssistancePlanHour(weeklyMinutes = 500, hourType = hourType, assistancePlan = it))
            assistancePlanRepository.save(it)
        }
        val otherHour = other.hours.first()
        val plan = savePlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))

        // When
        assistancePlanService.update(plan.id, updateRequest(plan.id).apply { hours = listOf(updateHour(otherHour.id, 20)) })

        // Then
        testEntityManager.flush()
        testEntityManager.clear()
        assertThat(assistancePlanRepository.findById(other.id).get().hours.map { it.weeklyMinutes }).containsExactly(500)
        assertThat(assistancePlanRepository.findById(plan.id).get().hours.map { it.weeklyMinutes }).containsExactly(20)
    }

    @Test
    fun update_existingMixedHours_allowsDeletingExistingHours() {
        // Given
        val plan = saveMixedPlan()
        val existingPlanHour = plan.hours.first()
        val existingGoal = plan.goals.first()

        val request = updateRequest(plan.id).apply {
            hours = listOf(updateHour(existingPlanHour.id, 120))
            goals = listOf(updateGoal(existingGoal.id, existingGoal.title))
        }

        // When
        val result = assistancePlanService.update(plan.id, request)

        // Then
        assertThat(result).isInstanceOf(AssistancePlanUpdateResult.Success::class.java)
        testEntityManager.flush()
        testEntityManager.clear()
        val saved = assistancePlanRepository.findById(plan.id).get()
        assertThat(saved.hours).hasSize(1)
        assertThat(saved.goals).hasSize(1)
        assertThat(saved.goals.first().hours).isEmpty()
    }

    @Test
    fun update_existingMixedHours_rejectsAddingNewHours() {
        // Given
        val plan = saveMixedPlan()
        val existingPlanHour = plan.hours.first()
        val existingGoal = plan.goals.first()
        val existingGoalHour = existingGoal.hours.first()

        val request = updateRequest(plan.id).apply {
            hours = listOf(updateHour(existingPlanHour.id, 120), updateHour(0, 30))
            goals = listOf(
                updateGoal(existingGoal.id, existingGoal.title, listOf(updateGoalHour(existingGoalHour.id, existingGoalHour.weeklyMinutes)))
            )
        }

        // When
        val result = assistancePlanService.update(plan.id, request)

        // Then
        assertThat(result).isEqualTo(
            AssistancePlanUpdateResult.InvalidHours(
                "Bei Hilfeplänen mit Stunden in beiden Bereichen dürfen keine neuen Stunden hinzugefügt werden. Bitte erst bestehende Stunden löschen, bis nur noch ein Bereich Stunden enthält."
            )
        )
    }

    @Test
    fun update_exactToCorridorMode_returnsHourModeChanged() {
        // Given
        val plan = savePlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
        val request = updateRequest(plan.id).apply {
            hourMode = AssistancePlanHourMode.CORRIDOR
            hourCorridorId = 1
        }

        // When / Then
        assertThat(assistancePlanService.update(plan.id, request)).isEqualTo(AssistancePlanUpdateResult.HourModeChanged)
    }

    @Test
    fun update_archivedClient_returnsClientArchived() {
        // Given
        val archivedClient = clientRepository.save(
            Client(firstName = "Erika", lastName = "Beispiel", categoryTemplate = categoryTemplate(), institution = institution, archived = true)
        )
        whenever(clientService.getEntityById(archivedClient.id)).thenReturn(archivedClient)
        val plan = savePlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))

        // When
        val result = assistancePlanService.update(plan.id, updateRequest(plan.id).apply { clientId = archivedClient.id })

        // Then
        assertThat(result).isEqualTo(AssistancePlanUpdateResult.ClientArchived)
    }

    @Test
    fun update_unknownReferenceAfterValidInput_leavesThePlanUntouched() {
        // Given
        val plan = savePlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
        val request = updateRequest(plan.id).apply {
            start = LocalDate.of(2020, 1, 1)
            hours = listOf(updateHour(0, 30, hourTypeId = 9999))
        }

        // When
        val result = assistancePlanService.update(plan.id, request)

        // Then
        assertThat(result).isEqualTo(AssistancePlanUpdateResult.HourTypeNotFound)
        testEntityManager.flush()
        testEntityManager.clear()
        assertThat(assistancePlanRepository.findById(plan.id).get().start).isEqualTo(LocalDate.of(2026, 1, 1))
    }

    @Test
    fun update_unknownAssistancePlan_returnsNotFound() {
        assertThat(assistancePlanService.update(9999, updateRequest(9999))).isEqualTo(AssistancePlanUpdateResult.NotFound)
    }

    @Test
    fun delete_existingAssistancePlan_removesPlanWithGoalsAndHours() {
        // Given
        val plan = saveMixedPlan()
        testEntityManager.flush()
        testEntityManager.clear()

        // When
        val result = assistancePlanService.delete(plan.id)

        // Then
        assertThat((result as AssistancePlanDeleteResult.Success).response.id).isEqualTo(plan.id)
        testEntityManager.flush()
        testEntityManager.clear()
        assertThat(assistancePlanRepository.findById(plan.id)).isEmpty
        assertThat(testEntityManager.entityManager.createQuery("select count(g) from Goal g").singleResult).isEqualTo(0L)
        assertThat(testEntityManager.entityManager.createQuery("select count(h) from AssistancePlanHour h").singleResult)
            .isEqualTo(0L)
    }

    @Test
    fun delete_unknownAssistancePlan_returnsNotFound() {
        assertThat(assistancePlanService.delete(9999)).isEqualTo(AssistancePlanDeleteResult.NotFound)
    }

    @Test
    fun getEditById_archivedClient_isHiddenUnlessIncludedOrLeading() {
        // Given
        val archivedClient = clientRepository.save(
            Client(firstName = "Erika", lastName = "Beispiel", categoryTemplate = categoryTemplate(), institution = institution, archived = true)
        )
        val plan = assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                client = archivedClient,
                sponsor = sponsor,
                institution = institution
            )
        )

        // When
        val hidden = assistancePlanService.getEditById(plan.id, includeArchived = false, leadingInstitutionIds = emptyList())
        val included = assistancePlanService.getEditById(plan.id, includeArchived = true, leadingInstitutionIds = emptyList())
        val leading = assistancePlanService.getEditById(
            plan.id,
            includeArchived = false,
            leadingInstitutionIds = listOf(institution.id!!)
        )

        // Then
        assertThat(hidden).isNull()
        assertThat(included!!.clientArchived).isTrue()
        assertThat(leading).isNotNull
    }

    @Test
    fun getEditById_returnsGoalsAndHours() {
        // Given
        val plan = saveMixedPlan()
        testEntityManager.flush()
        testEntityManager.clear()

        // When
        val result = assistancePlanService.getEditById(plan.id, includeArchived = false, leadingInstitutionIds = emptyList())!!

        // Then
        assertThat(result.institutionName).isEqualTo("Inst")
        assertThat(result.hours.map { it.weeklyMinutes }).containsExactly(120)
        assertThat(result.goals.single().hours.map { it.weeklyMinutes }).containsExactly(60)
    }

    @Test
    fun getAllEditResponsesByYearAndInstitutionIdAndSponsorId_filtersByYearInstitutionAndSponsor() {
        // Given
        val otherSponsor = sponsorRepository.save(Sponsor(name = "Other", payOverhang = true, payExact = false))
        val otherInstitution = institutionRepository.save(Institution(name = "Other", email = "a@b.c", phonenumber = "2"))
        val inYear = savePlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
        savePlan(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
        val otherSponsorPlan = assistancePlanRepository.save(
            AssistancePlan(start = LocalDate.of(2026, 1, 1), end = LocalDate.of(2026, 6, 30), client = client, sponsor = otherSponsor, institution = institution)
        )
        val otherInstitutionPlan = assistancePlanRepository.save(
            AssistancePlan(start = LocalDate.of(2026, 1, 1), end = LocalDate.of(2026, 6, 30), client = client, sponsor = sponsor, institution = otherInstitution)
        )

        // When / Then
        assertThat(assistancePlanService.getAllEditResponsesByYearAndInstitutionIdAndSponsorId(2026, null, null).map { it.id })
            .containsExactlyInAnyOrder(inYear.id, otherSponsorPlan.id, otherInstitutionPlan.id)
        assertThat(assistancePlanService.getAllEditResponsesByYearAndInstitutionIdAndSponsorId(2026, institution.id, null).map { it.id })
            .containsExactlyInAnyOrder(inYear.id, otherSponsorPlan.id)
        assertThat(assistancePlanService.getAllEditResponsesByYearAndInstitutionIdAndSponsorId(2026, null, sponsor.id).map { it.id })
            .containsExactlyInAnyOrder(inYear.id, otherInstitutionPlan.id)
        assertThat(assistancePlanService.getAllEditResponsesByYearAndInstitutionIdAndSponsorId(2026, institution.id, sponsor.id).map { it.id })
            .containsExactly(inYear.id)
    }

    @Test
    fun getAllEntitiesByClientId_sortsByStartDesc() {
        // Given
        savePlan(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
        savePlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
        savePlan(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31))

        // When
        val result = assistancePlanService.getAllEntitiesByClientId(client.id)

        // Then
        assertThat(result.map { it.start }).containsExactly(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2024, 1, 1)
        )
    }

    @Test
    fun getAllForServiceEditingByClientId_filtersByInstitutionAndListsDocumentationHourTypes() {
        // Given
        val otherInstitution = institutionRepository.save(Institution(name = "Inst B", email = "b@b.c", phonenumber = "2"))
        val hourTypeB = hourTypeRepository.save(HourType(title = "Indirekt", price = 20.0))
        val hourTypeC = hourTypeRepository.save(HourType(title = "Beratung", price = 30.0))
        val corridor = hourCorridorRepository.save(
            HourCorridor(title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourType = hourType)
        )
        val included = AssistancePlan(
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 6, 30),
            client = client,
            sponsor = sponsor,
            institution = institution,
            hourMode = AssistancePlanHourMode.CORRIDOR,
            hourCorridor = corridor
        )
        included.hours.add(AssistancePlanHour(weeklyMinutes = 60, hourType = hourType, assistancePlan = included))
        included.hours.add(AssistancePlanHour(weeklyMinutes = 30, hourType = hourTypeB, assistancePlan = included))
        included.hours.add(AssistancePlanHour(weeklyMinutes = 20, hourType = hourTypeC, assistancePlan = included))
        val goal = Goal(title = "Ziel", description = "Beschreibung", institution = institution, assistancePlan = included)
        goal.hours.add(GoalHour(weeklyMinutes = 15, hourType = hourTypeB, goal = goal))
        included.goals.add(goal)
        assistancePlanRepository.save(included)
        assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 7, 1),
                end = LocalDate.of(2026, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = otherInstitution
            )
        )
        testEntityManager.flush()
        testEntityManager.clear()

        // When
        val result = assistancePlanService.getAllForServiceEditingByClientId(client.id, listOf(institution.id!!))

        // Then
        val plan = result.single()
        assertThat(plan.institutionId).isEqualTo(institution.id)
        assertThat(plan.institutionName).isEqualTo("Inst")
        assertThat(plan.clientId).isEqualTo(client.id)
        assertThat(plan.hourMode).isEqualTo(AssistancePlanHourMode.CORRIDOR)
        assertThat(plan.hourCorridorId).isEqualTo(corridor.id)
        assertThat(plan.goals.single().hours).hasSize(1)
        assertThat(plan.hours).hasSize(3)
        assertThat(plan.possibleDocumentationHourTypes.map { it.title })
            .containsExactly("Beratung", "Indirekt", "Standard")
    }

    @Test
    fun getAllForServiceEditingByClientId_sortsByStartAscending() {
        // Given
        savePlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
        savePlan(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))

        // When
        val result = assistancePlanService.getAllForServiceEditingByClientId(client.id, listOf(institution.id!!))

        // Then
        assertThat(result.map { it.start }).containsExactly(LocalDate.of(2024, 1, 1), LocalDate.of(2026, 1, 1))
    }

    @Test
    fun existsById_reflectsStoredPlans() {
        // Given
        val plan = savePlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))

        // When / Then
        assertThat(assistancePlanService.existsById(plan.id)).isTrue()
        assertThat(assistancePlanService.existsById(9999)).isFalse()
    }

    private fun categoryTemplate(): CategoryTemplate =
        categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))

    private fun savePlan(start: LocalDate, end: LocalDate): AssistancePlan =
        assistancePlanRepository.save(
            AssistancePlan(start = start, end = end, client = client, sponsor = sponsor, institution = institution)
        )

    private fun saveMixedPlan(): AssistancePlan {
        val plan = AssistancePlan(
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 12, 31),
            client = client,
            sponsor = sponsor,
            institution = institution
        )
        plan.hours = mutableSetOf(AssistancePlanHour(weeklyMinutes = 120, hourType = hourType, assistancePlan = plan))
        val goal = Goal(title = "Goal 1", description = "Description", institution = institution, assistancePlan = plan)
        goal.hours = mutableSetOf(GoalHour(weeklyMinutes = 60, hourType = hourType, goal = goal))
        plan.goals = mutableSetOf(goal)

        return assistancePlanRepository.save(plan)
    }

    private fun createRequest() = AssistancePlanCreateRequest().apply {
        start = LocalDate.of(2026, 1, 1)
        end = LocalDate.of(2026, 12, 31)
        clientId = client.id
        institutionId = institution.id!!
        sponsorId = sponsor.id
    }

    private fun createHour(weeklyMinutes: Int, hourTypeId: Long = hourType.id) = AssistancePlanCreateHourRequest().apply {
        this.weeklyMinutes = weeklyMinutes
        this.hourTypeId = hourTypeId
    }

    private fun createGoal(title: String, hours: List<AssistancePlanCreateHourRequest> = emptyList()) =
        AssistancePlanCreateGoalRequest().apply {
            this.title = title
            description = "Description"
            institutionId = institution.id
            this.hours = hours
        }

    private fun updateRequest(id: Long) = AssistancePlanUpdateRequest().apply {
        this.id = id
        start = LocalDate.of(2026, 1, 1)
        end = LocalDate.of(2026, 12, 31)
        clientId = client.id
        institutionId = institution.id!!
        sponsorId = sponsor.id
    }

    private fun updateHour(id: Long, weeklyMinutes: Int, hourTypeId: Long = hourType.id) =
        AssistancePlanUpdateHourRequest().apply {
            this.id = id
            this.weeklyMinutes = weeklyMinutes
            this.hourTypeId = hourTypeId
        }

    private fun updateGoal(id: Long, title: String, hours: List<AssistancePlanUpdateGoalHourRequest> = emptyList()) =
        AssistancePlanUpdateGoalRequest().apply {
            this.id = id
            this.title = title
            description = "Description"
            institutionId = institution.id
            this.hours = hours
        }

    private fun updateGoalHour(id: Long, weeklyMinutes: Int) = AssistancePlanUpdateGoalHourRequest().apply {
        this.id = id
        this.weeklyMinutes = weeklyMinutes
        hourTypeId = hourType.id
    }
}
