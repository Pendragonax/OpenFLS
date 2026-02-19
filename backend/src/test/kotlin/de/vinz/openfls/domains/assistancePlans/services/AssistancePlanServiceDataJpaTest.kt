package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanCreateDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanUpdateDto
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanHourRepository
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.hourTypes.HourTypeRepository
import de.vinz.openfls.domains.hourTypes.HourTypeService
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.domains.services.services.ServiceService
import de.vinz.openfls.domains.sponsors.Sponsor
import de.vinz.openfls.domains.sponsors.SponsorRepository
import de.vinz.openfls.domains.sponsors.SponsorService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
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
    lateinit var assistancePlanHourRepository: AssistancePlanHourRepository

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

    @MockitoBean
    lateinit var clientService: ClientService

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var sponsorService: SponsorService

    @MockitoBean
    lateinit var hourTypeService: HourTypeService

    @MockitoBean
    lateinit var serviceService: ServiceService

    @Test
    fun create_validCreateDto_withGoalHoursOnly_persistsGoalsAndGoalHours() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", categoryTemplate = categoryTemplate, institution = institution))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))

        whenever(clientService.getById(client.id)).thenReturn(client)
        whenever(institutionService.getEntityById(institution.id!!)).thenReturn(institution)
        whenever(sponsorService.getById(sponsor.id)).thenReturn(sponsor)
        whenever(hourTypeService.getById(hourType.id)).thenReturn(hourType)

        val createDto = AssistancePlanCreateDto().apply {
            start = LocalDate.of(2026, 1, 1)
            end = LocalDate.of(2026, 12, 31)
            clientId = client.id
            institutionId = institution.id!!
            sponsorId = sponsor.id
            hours = emptyList()
            goals = listOf(
                de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanCreateGoalDto().apply {
                    title = "Goal 1"
                    description = "Description"
                    institutionId = institution.id
                    hours = listOf(
                        de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanCreateHourDto().apply {
                            weeklyMinutes = 60
                            hourTypeId = hourType.id
                        }
                    )
                }
            )
        }

        // When
        val result = assistancePlanService.create(createDto)

        // Then
        val saved = assistancePlanRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().hours).isEmpty()
        assertThat(saved.get().goals).hasSize(1)
        assertThat(saved.get().goals.first().hours).hasSize(1)
    }

    @Test
    fun create_createDtoWithPlanHoursAndGoalHours_throwsGermanValidationMessage() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", categoryTemplate = categoryTemplate, institution = institution))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))

        whenever(clientService.getById(client.id)).thenReturn(client)
        whenever(institutionService.getEntityById(institution.id!!)).thenReturn(institution)
        whenever(sponsorService.getById(sponsor.id)).thenReturn(sponsor)
        whenever(hourTypeService.getById(hourType.id)).thenReturn(hourType)

        val createDto = AssistancePlanCreateDto().apply {
            start = LocalDate.of(2026, 1, 1)
            end = LocalDate.of(2026, 12, 31)
            clientId = client.id
            institutionId = institution.id!!
            sponsorId = sponsor.id
            hours = listOf(
                de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanCreateHourDto().apply {
                    weeklyMinutes = 120
                    hourTypeId = hourType.id
                }
            )
            goals = listOf(
                de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanCreateGoalDto().apply {
                    title = "Goal 1"
                    description = "Description"
                    institutionId = institution.id
                    hours = listOf(
                        de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanCreateHourDto().apply {
                            weeklyMinutes = 60
                            hourTypeId = hourType.id
                        }
                    )
                }
            )
        }

        // When / Then
        assertThatThrownBy { assistancePlanService.create(createDto) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Stunden dürfen entweder direkt im Hilfeplan oder in den Zielen hinterlegt sein, nicht in beiden Bereichen gleichzeitig.")
    }

    @Test
    fun getAssistancePlanDtosByClientId_sortsByStartDesc() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", categoryTemplate = categoryTemplate, institution = institution))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))

        assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2024, 1, 1),
                end = LocalDate.of(2024, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = institution
            )
        )
        assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = institution
            )
        )
        assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2025, 1, 1),
                end = LocalDate.of(2025, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = institution
            )
        )

        // When
        val result = assistancePlanService.getAssistancePlanDtosByClientId(client.id)

        // Then
        assertThat(result).hasSize(3)
        assertThat(result.map { it.start }).containsExactly(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2024, 1, 1)
        )
        assertThat(result.map { it.institutionName }).containsOnly("Inst")
    }

    @Test
    fun update_updateDtoWithGoalHours_updatesGoalAndGoalHour() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", categoryTemplate = categoryTemplate, institution = institution))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))

        whenever(clientService.getById(client.id)).thenReturn(client)
        whenever(institutionService.getEntityById(institution.id!!)).thenReturn(institution)
        whenever(sponsorService.getById(sponsor.id)).thenReturn(sponsor)
        whenever(hourTypeService.getById(hourType.id)).thenReturn(hourType)

        val created = assistancePlanService.create(AssistancePlanCreateDto().apply {
            start = LocalDate.of(2026, 1, 1)
            end = LocalDate.of(2026, 12, 31)
            clientId = client.id
            institutionId = institution.id!!
            sponsorId = sponsor.id
            goals = listOf(
                de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanCreateGoalDto().apply {
                    title = "Goal Old"
                    description = "Desc Old"
                    institutionId = institution.id
                    hours = listOf(
                        de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanCreateHourDto().apply {
                            weeklyMinutes = 60
                            hourTypeId = hourType.id
                        }
                    )
                }
            )
        })

        val createdEntity = assistancePlanRepository.findById(created.id).orElseThrow()
        val existingGoal = createdEntity.goals.first()
        val existingGoalHour = existingGoal.hours.first()

        val updateDto = AssistancePlanUpdateDto().apply {
            id = created.id
            start = LocalDate.of(2026, 2, 1)
            end = LocalDate.of(2026, 11, 30)
            clientId = client.id
            institutionId = institution.id!!
            sponsorId = sponsor.id
            hours = emptyList()
            goals = listOf(
                de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanUpdateGoalDto().apply {
                    id = existingGoal.id
                    title = "Goal New"
                    description = "Desc New"
                    assistancePlanId = created.id
                    institutionId = institution.id
                    hours = listOf(
                        de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanUpdateGoalHourDto().apply {
                            id = existingGoalHour.id
                            weeklyMinutes = 90
                            hourTypeId = hourType.id
                            goalHourId = existingGoal.id
                        }
                    )
                }
            )
        }

        // When
        val result = assistancePlanService.update(created.id, updateDto)

        // Then
        val saved = assistancePlanRepository.findById(result.id).orElseThrow()
        assertThat(saved.start).isEqualTo(LocalDate.of(2026, 2, 1))
        assertThat(saved.end).isEqualTo(LocalDate.of(2026, 11, 30))
        assertThat(saved.goals).hasSize(1)
        assertThat(saved.goals.first().id).isEqualTo(existingGoal.id)
        assertThat(saved.goals.first().title).isEqualTo("Goal New")
        assertThat(saved.goals.first().hours).hasSize(1)
        assertThat(saved.goals.first().hours.first().id).isEqualTo(existingGoalHour.id)
        assertThat(saved.goals.first().hours.first().weeklyMinutes).isEqualTo(90)
    }
}
