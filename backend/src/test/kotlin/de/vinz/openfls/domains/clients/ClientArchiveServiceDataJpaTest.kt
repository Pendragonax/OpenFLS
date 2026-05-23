package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.categories.CategoryTemplateService
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
import de.vinz.openfls.domains.clients.archive.ClientArchiveActionType
import de.vinz.openfls.domains.clients.archive.ClientArchiveActor
import de.vinz.openfls.domains.clients.archive.ClientArchiveService
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.exceptions.UserNotAllowedException
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

@DataJpaTest
@Import(ClientArchiveService::class, ClientService::class, TestBeans::class)
class ClientArchiveServiceDataJpaTest {

    @Autowired
    lateinit var clientArchiveService: ClientArchiveService

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Autowired
    lateinit var categoryTemplateRepository: CategoryTemplateRepository

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var categoryTemplateService: CategoryTemplateService

    @Test
    fun archive_withLeadPermission_persistsHistoryEntry() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate))
        val actor = ClientArchiveActor(
            employeeId = 8,
            firstname = "Anna",
            lastname = "Lead",
            isAdmin = false,
            leadingInstitutionIds = listOf(institution.id!!)
        )

        // When
        val entry = clientArchiveService.archive(
            clientId = client.id,
            actionDate = LocalDate.of(2026, 5, 23),
            reason = "Archived by request",
            remark = "Initial archive",
            actor = actor
        )

        // Then
        val saved = clientRepository.findById(client.id)
        assertThat(saved).isPresent
        assertThat(saved.get().archived).isTrue
        assertThat(saved.get().archiveHistoryEntries).hasSize(1)
        assertThat(entry.actionType).isEqualTo(ClientArchiveActionType.ARCHIVE)
        assertThat(entry.executingEmployeeFirstname).isEqualTo("Anna")
        assertThat(entry.executingEmployeeLastname).isEqualTo("Lead")
    }

    @Test
    fun archive_withoutLeadPermission_throwsUserNotAllowedException() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val otherInstitution = institutionRepository.save(Institution(name = "Other", email = "x@y.z", phonenumber = "2"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate))
        val actor = ClientArchiveActor(
            employeeId = 8,
            firstname = "Anna",
            lastname = "Employee",
            isAdmin = false,
            leadingInstitutionIds = listOf(otherInstitution.id!!)
        )

        // When / Then
        assertThatThrownBy {
            clientArchiveService.archive(
                clientId = client.id,
                actionDate = LocalDate.of(2026, 5, 23),
                reason = "Archived by request",
                remark = "Initial archive",
                actor = actor
            )
        }.isInstanceOf(UserNotAllowedException::class.java)
    }

    @Test
    fun reactivate_withLeadPermission_restoresArchiveState() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        whenever(institutionService.getEntityById(any())).thenReturn(institution)
        whenever(categoryTemplateService.getById(any())).thenReturn(categoryTemplate)
        val client = clientRepository.save(Client(firstName = "Max", lastName = "Mustermann", institution = institution, categoryTemplate = categoryTemplate))
        val actor = ClientArchiveActor(
            employeeId = 8,
            firstname = "Anna",
            lastname = "Lead",
            isAdmin = false,
            leadingInstitutionIds = listOf(institution.id!!)
        )
        clientArchiveService.archive(
            clientId = client.id,
            actionDate = LocalDate.of(2026, 5, 23),
            reason = "Archived by request",
            remark = "Initial archive",
            actor = actor
        )

        // When
        val entry = clientArchiveService.reactivate(
            clientId = client.id,
            actionDate = LocalDate.of(2026, 5, 23),
            reason = "Client active again",
            remark = "Reactivated",
            actor = actor
        )

        // Then
        val saved = clientRepository.findById(client.id)
        assertThat(saved).isPresent
        assertThat(saved.get().archived).isFalse
        assertThat(saved.get().archiveHistoryEntries).hasSize(2)
        assertThat(entry.actionType).isEqualTo(ClientArchiveActionType.REACTIVATE)
        assertThat(clientArchiveService.getArchiveHistory(client.id)).hasSize(2)
    }
}
