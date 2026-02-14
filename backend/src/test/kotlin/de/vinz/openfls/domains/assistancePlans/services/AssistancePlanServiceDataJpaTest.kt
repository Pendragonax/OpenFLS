package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanCreateDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourDto
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
    fun create_validDto_persistsPlanAndHours() {
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

        val dto = AssistancePlanDto().apply {
            start = LocalDate.of(2026, 1, 1)
            end = LocalDate.of(2026, 12, 31)
            clientId = client.id
            institutionId = institution.id!!
            sponsorId = sponsor.id
            hours = mutableSetOf(AssistancePlanHourDto().apply {
                weeklyMinutes = 600
                hourTypeId = hourType.id
            })
        }

        // When
        val result = assistancePlanService.create(dto)

        // Then
        val saved = assistancePlanRepository.findById(result.id)
        assertThat(saved).isPresent
        val hours = assistancePlanHourRepository.findByAssistancePlanId(result.id)
        assertThat(hours).hasSize(1)
    }

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
    fun update_idMismatch_throwsException() {
        // Given
        val dto = AssistancePlanDto().apply { id = 1 }

        // When / Then
        assertThatThrownBy { assistancePlanService.update(2, dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_missingAssistancePlan_throwsException() {
        // Given
        val dto = AssistancePlanDto().apply { id = 9999 }

        // When / Then
        assertThatThrownBy { assistancePlanService.update(9999, dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_existingPlan_updatesHours() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", categoryTemplate = categoryTemplate, institution = institution))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 5.0))

        val existing = assistancePlanRepository.save(AssistancePlan(start = LocalDate.of(2026, 1, 1), end = LocalDate.of(2026, 12, 31), client = client, sponsor = sponsor, institution = institution))

        whenever(clientService.getById(client.id)).thenReturn(client)
        whenever(institutionService.getEntityById(institution.id!!)).thenReturn(institution)
        whenever(sponsorService.getById(sponsor.id)).thenReturn(sponsor)
        whenever(hourTypeService.getById(hourType.id)).thenReturn(hourType)

        val dto = AssistancePlanDto().apply {
            id = existing.id
            start = LocalDate.of(2026, 1, 1)
            end = LocalDate.of(2026, 12, 31)
            clientId = client.id
            institutionId = institution.id!!
            sponsorId = sponsor.id
            hours = mutableSetOf(AssistancePlanHourDto().apply {
                weeklyMinutes = 720
                hourTypeId = hourType.id
            })
        }

        // When
        val result = assistancePlanService.update(existing.id, dto)

        // Then
        val saved = assistancePlanRepository.findById(result.id)
        assertThat(saved).isPresent
        val hours = assistancePlanHourRepository.findByAssistancePlanId(result.id)
        assertThat(hours).isNotNull
    }
}
