package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.categories.CategoryTemplateService
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
import de.vinz.openfls.domains.clients.archive.ClientArchiveActionType
import de.vinz.openfls.domains.clients.dtos.ClientDto
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.goals.entities.GoalHour
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.hourTypes.HourTypeRepository
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.domains.sponsors.Sponsor
import de.vinz.openfls.domains.sponsors.SponsorRepository
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest
@Import(ClientService::class, TestBeans::class)
class ClientServiceDataJpaTest {

    @Autowired
    lateinit var clientService: ClientService

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Autowired
    lateinit var categoryTemplateRepository: CategoryTemplateRepository

    @Autowired
    lateinit var sponsorRepository: SponsorRepository

    @Autowired
    lateinit var hourTypeRepository: HourTypeRepository

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var categoryTemplateService: CategoryTemplateService

    @Test
    fun create_validDto_persistsEntity() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)

        val dto = ClientDto().apply {
            firstName = "Max"
            lastName = "Mustermann"
            institution.id = institution.id!!
            categoryTemplate.id = categoryTemplate.id
        }

        // When
        val result = clientService.create(dto)

        // Then
        val saved = clientRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().firstName).isEqualTo("Max")
    }

    @Test
    fun create_missingInstitution_throwsException() {
        // Given
        whenever(institutionService.getEntityById(any())).thenReturn(null)
        val dto = ClientDto().apply {
            firstName = "Max"
            lastName = "Mustermann"
            institution.id = 9999
        }

        // When / Then
        assertThatThrownBy { clientService.create(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_missingClient_throwsException() {
        // Given
        val dto = ClientDto().apply {
            id = 9999
            firstName = "Max"
            lastName = "Mustermann"
        }

        // When / Then
        assertThatThrownBy { clientService.update(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_existingClient_updatesFields() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val existing = clientRepository.save(Client(firstName = "Old", lastName = "Name", institution = institution, categoryTemplate = categoryTemplate))

        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)

        val dto = ClientDto().apply {
            id = existing.id
            firstName = "New"
            lastName = "Name"
            institution.id = institution.id!!
            categoryTemplate.id = categoryTemplate.id
        }

        // When
        val result = clientService.update(dto)

        // Then
        val saved = clientRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().firstName).isEqualTo("New")
    }

    @Test
    fun update_archivedClient_throwsException() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val existing = clientRepository.save(
            Client(firstName = "Old", lastName = "Name", institution = institution, categoryTemplate = categoryTemplate, archived = true)
        )
        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)

        val dto = ClientDto().apply {
            id = existing.id
            firstName = "New"
            lastName = "Name"
            institution.id = institution.id!!
            categoryTemplate.id = categoryTemplate.id
        }

        // When / Then
        assertThatThrownBy { clientService.update(dto) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("client is archived")
    }

    @Test
    fun getDtoById_setsInstitutionNameForAssistancePlans() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst A", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))
        val client = clientRepository.save(
            Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate)
        )

        client.assistancePlans.add(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = institution
            )
        )
        clientRepository.save(client)

        // When
        val result = clientService.getDtoById(client.id)

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.assistancePlans).hasSize(1)
        assertThat(result.assistancePlans.first().institutionName).isEqualTo("Inst A")
    }

    @Test
    fun getDtoById_filtersArchivedClientsUnlessIncluded() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val archivedClient = clientRepository.save(
            Client(firstName = "Archived", lastName = "Client", institution = institution, categoryTemplate = categoryTemplate, archived = true)
        )

        // When
        val hiddenResult = clientService.getDtoById(archivedClient.id)
        val visibleResult = clientService.getDtoById(archivedClient.id, includeArchived = true)

        // Then
        assertThat(hiddenResult).isNull()
        assertThat(visibleResult).isNotNull
        assertThat(visibleResult!!.archived).isTrue()
    }

    @Test
    fun getAllClientSimpleDto_filtersArchivedClientsUnlessIncluded() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        clientRepository.save(Client(firstName = "Active", lastName = "Client", institution = institution, categoryTemplate = categoryTemplate))
        clientRepository.save(Client(firstName = "Archived", lastName = "Client", institution = institution, categoryTemplate = categoryTemplate, archived = true))

        // When
        val hiddenResult = clientService.getAllClientSimpleDto()
        val visibleResult = clientService.getAllClientSimpleDto(includeArchived = true)

        // Then
        assertThat(hiddenResult).hasSize(1)
        assertThat(hiddenResult.first().firstName).isEqualTo("Active")
        assertThat(visibleResult).hasSize(2)
    }

    @Test
    fun getAllClientSoloDto_filtersArchivedClientsUnlessIncluded() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        clientRepository.save(Client(firstName = "Active", lastName = "Client", institution = institution, categoryTemplate = categoryTemplate))
        clientRepository.save(Client(firstName = "Archived", lastName = "Client", institution = institution, categoryTemplate = categoryTemplate, archived = true))

        // When
        val hiddenResult = clientService.getAllClientSoloDto()
        val visibleResult = clientService.getAllClientSoloDto(includeArchived = true)

        // Then
        assertThat(hiddenResult).hasSize(1)
        assertThat(hiddenResult.first().firstName).isEqualTo("Active")
        assertThat(visibleResult).hasSize(2)
    }

    @Test
    fun getForServiceEditingById_existingClient_filtersAssistancePlansByAllowedInstitutions() {
        // Given
        val institutionA = institutionRepository.save(Institution(name = "Inst A", email = "a@b.c", phonenumber = "1"))
        val institutionB = institutionRepository.save(Institution(name = "Inst B", email = "b@b.c", phonenumber = "2"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))
        val client = clientRepository.save(
            Client(firstName = "Max", lastName = "Mustermann", institution = institutionA, categoryTemplate = categoryTemplate)
        )

        val includedPlan = AssistancePlan(
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 6, 30),
            client = client,
            sponsor = sponsor,
            institution = institutionA
        )
        val hourTypeA = hourTypeRepository.save(HourType(title = "Direkt", price = 10.0))
        val hourTypeB = hourTypeRepository.save(HourType(title = "Indirekt", price = 20.0))
        val hourTypeC = hourTypeRepository.save(HourType(title = "Beratung", price = 30.0))

        includedPlan.hours.add(AssistancePlanHour(weeklyMinutes = 60, hourType = hourTypeA, assistancePlan = includedPlan))
        includedPlan.hours.add(AssistancePlanHour(weeklyMinutes = 30, hourType = hourTypeB, assistancePlan = includedPlan))
        includedPlan.hours.add(AssistancePlanHour(weeklyMinutes = 20, hourType = hourTypeC, assistancePlan = includedPlan))
        val goal = Goal(title = "Ziel", description = "Beschreibung", institution = institutionA, assistancePlan = includedPlan)
        goal.hours.add(GoalHour(weeklyMinutes = 15, hourType = hourTypeB, goal = goal))
        includedPlan.goals.add(goal)
        client.assistancePlans.add(includedPlan)
        client.assistancePlans.add(
            AssistancePlan(
                start = LocalDate.of(2026, 7, 1),
                end = LocalDate.of(2026, 12, 31),
                client = client,
                sponsor = sponsor,
                institution = institutionB
            )
        )
        clientRepository.save(client)

        // When
        val result = clientService.getForServiceEditingById(client.id, listOf(institutionA.id!!))

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.assistancePlans).hasSize(1)
        assertThat(result.assistancePlans.first().institutionId).isEqualTo(institutionA.id)
        assertThat(result.assistancePlans.first().institutionName).isEqualTo("Inst A")
        assertThat(result.assistancePlans.first().possibleDocumentationHourTypes.map { it.title })
            .containsExactlyInAnyOrder("Beratung", "Direkt", "Indirekt")
    }

    @Test
    fun getForServiceEditingById_filtersArchivedClientsUnlessIncluded() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))
        val archivedClient = clientRepository.save(
            Client(firstName = "Archived", lastName = "Client", institution = institution, categoryTemplate = categoryTemplate, archived = true)
        )
        archivedClient.assistancePlans.add(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                client = archivedClient,
                sponsor = sponsor,
                institution = institution
            )
        )
        clientRepository.save(archivedClient)

        // When
        val hiddenResult = clientService.getForServiceEditingById(archivedClient.id, listOf(institution.id!!))
        val visibleResult = clientService.getForServiceEditingById(
            archivedClient.id,
            listOf(institution.id!!),
            includeArchived = true
        )

        // Then
        assertThat(hiddenResult).isNull()
        assertThat(visibleResult).isNotNull
        assertThat(visibleResult!!.assistancePlans).hasSize(1)
    }

    @Test
    fun getForServiceEditingById_missingClient_returnsNull() {
        // Given
        val missingId = 99999L

        // When
        val result = clientService.getForServiceEditingById(missingId, listOf(1L, 2L))

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun archive_createsHistoryEntry_andMarksClientArchived() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val client = clientRepository.save(
            Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate)
        )
        val actionDate = LocalDate.of(2026, 5, 23)
        val actionTimestamp = LocalDateTime.of(2026, 5, 23, 10, 15)

        // When
        clientService.archive(
            clientId = client.id,
            actionDate = actionDate,
            actionTimestamp = actionTimestamp,
            executingEmployeeId = 11,
            executingEmployeeFirstname = "Anna",
            executingEmployeeLastname = "Lead",
            reason = "Archived by request",
            remark = "Initial archive"
        )

        // Then
        val saved = clientRepository.findById(client.id)
        assertThat(saved).isPresent
        assertThat(saved.get().archived).isTrue
        assertThat(saved.get().archiveHistoryEntries).hasSize(1)
        assertThat(saved.get().archiveHistoryEntries.first().actionType).isEqualTo(ClientArchiveActionType.ARCHIVE)
        assertThat(saved.get().archiveHistoryEntries.first().actionDate).isEqualTo(actionDate)
        assertThat(saved.get().archiveHistoryEntries.first().actionTimestamp).isEqualTo(actionTimestamp)
        assertThat(saved.get().archiveHistoryEntries.first().executingEmployeeId).isEqualTo(11)
        assertThat(saved.get().archiveHistoryEntries.first().executingEmployeeFirstname).isEqualTo("Anna")
        assertThat(saved.get().archiveHistoryEntries.first().executingEmployeeLastname).isEqualTo("Lead")
        assertThat(saved.get().archiveHistoryEntries.first().reason).isEqualTo("Archived by request")
        assertThat(saved.get().archiveHistoryEntries.first().remark).isEqualTo("Initial archive")
    }

    @Test
    fun reactivate_appendsHistoryEntry_andReturnsNewestHistoryFirst() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val client = clientRepository.save(
            Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate)
        )
        val archiveTimestamp = LocalDateTime.of(2026, 5, 23, 9, 0)
        val reactivateTimestamp = LocalDateTime.of(2026, 5, 23, 11, 0)
        clientService.archive(
            clientId = client.id,
            actionDate = LocalDate.of(2026, 5, 23),
            actionTimestamp = archiveTimestamp,
            executingEmployeeId = 11,
            executingEmployeeFirstname = "Anna",
            executingEmployeeLastname = "Lead",
            reason = "Archived by request",
            remark = "Initial archive"
        )

        // When
        clientService.reactivate(
            clientId = client.id,
            actionDate = LocalDate.of(2026, 5, 23),
            actionTimestamp = reactivateTimestamp,
            executingEmployeeId = 12,
            executingEmployeeFirstname = "Ben",
            executingEmployeeLastname = "Lead",
            reason = "Client active again",
            remark = "Reactivated"
        )

        // Then
        val saved = clientRepository.findById(client.id)
        assertThat(saved).isPresent
        assertThat(saved.get().archived).isFalse
        assertThat(saved.get().archiveHistoryEntries).hasSize(2)

        val history = clientService.getArchiveHistoryById(client.id)
        assertThat(history).hasSize(2)
        assertThat(history.first().actionType).isEqualTo(ClientArchiveActionType.REACTIVATE)
        assertThat(history.first().actionTimestamp).isEqualTo(reactivateTimestamp)
        assertThat(history[1].actionType).isEqualTo(ClientArchiveActionType.ARCHIVE)
        assertThat(history[1].actionTimestamp).isEqualTo(archiveTimestamp)
    }

    @Test
    fun archive_duplicateStateChange_throwsException() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val client = clientRepository.save(
            Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate)
        )
        clientService.archive(
            clientId = client.id,
            actionDate = LocalDate.of(2026, 5, 23),
            actionTimestamp = LocalDateTime.of(2026, 5, 23, 9, 0),
            executingEmployeeId = 11,
            executingEmployeeFirstname = "Anna",
            executingEmployeeLastname = "Lead",
            reason = "Archived by request",
            remark = "Initial archive"
        )

        // When / Then
        assertThatThrownBy {
            clientService.archive(
                clientId = client.id,
                actionDate = LocalDate.of(2026, 5, 23),
                actionTimestamp = LocalDateTime.of(2026, 5, 23, 10, 0),
                executingEmployeeId = 11,
                executingEmployeeFirstname = "Anna",
                executingEmployeeLastname = "Lead",
                reason = "Archived again",
                remark = "Should fail"
            )
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun reactivate_activeClient_throwsException() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val client = clientRepository.save(
            Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate)
        )

        // When / Then
        assertThatThrownBy {
            clientService.reactivate(
                clientId = client.id,
                actionDate = LocalDate.of(2026, 5, 23),
                actionTimestamp = LocalDateTime.of(2026, 5, 23, 10, 0),
                executingEmployeeId = 12,
                executingEmployeeFirstname = "Ben",
                executingEmployeeLastname = "Lead",
                reason = "Reactivated",
                remark = "Should fail"
            )
        }.isInstanceOf(IllegalStateException::class.java)
    }
}
