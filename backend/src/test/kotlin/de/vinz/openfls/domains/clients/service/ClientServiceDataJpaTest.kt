package de.vinz.openfls.domains.clients.service

import de.vinz.openfls.domains.categories.entity.CategoryTemplate
import de.vinz.openfls.domains.categories.repository.CategoryTemplateRepository
import de.vinz.openfls.domains.categories.service.CategoryTemplateService
import de.vinz.openfls.domains.clients.dto.ClientCreateRequest
import de.vinz.openfls.domains.clients.dto.ClientCreateResult
import de.vinz.openfls.domains.clients.dto.ClientUpdateRequest
import de.vinz.openfls.domains.clients.dto.ClientUpdateResult
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.clients.repository.ClientRepository
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.testsupport.TestBeans
import de.vinz.openfls.testsupport.QueryCounter
import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean

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
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var categoryTemplateService: CategoryTemplateService

    private lateinit var institution: Institution
    private lateinit var categoryTemplate: CategoryTemplate

    @BeforeEach
    fun setUp() {
        institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        categoryTemplate = categoryTemplateRepository.save(
            CategoryTemplate(title = "Template", description = "", withoutClient = false)
        )
        whenever(institutionService.getEntityById(institution.id!!)).thenReturn(institution)
        whenever(categoryTemplateService.getEntityById(categoryTemplate.id)).thenReturn(categoryTemplate)
    }

    @Test
    fun create_validRequest_persistsClientWithInstitutionAndCategoryTemplate() {
        // When
        val result = clientService.create(createRequest())

        // Then
        val response = (result as ClientCreateResult.Success).response
        val saved = clientRepository.findById(response.id).get()
        assertThat(saved.firstName).isEqualTo("Max")
        assertThat(saved.archived).isFalse()
        assertThat(response.institution.name).isEqualTo("Inst")
        assertThat(response.categoryTemplateTitle).isEqualTo("Template")
    }

    @Test
    fun create_unknownInstitution_returnsInstitutionNotFound() {
        // When
        val result = clientService.create(createRequest().apply { institutionId = 9999 })

        // Then
        assertThat(result).isEqualTo(ClientCreateResult.InstitutionNotFound)
        assertThat(clientRepository.count()).isZero()
    }

    @Test
    fun create_unknownCategoryTemplate_returnsCategoryTemplateNotFound() {
        // When
        val result = clientService.create(createRequest().apply { categoryTemplateId = 9999 })

        // Then
        assertThat(result).isEqualTo(ClientCreateResult.CategoryTemplateNotFound)
        assertThat(clientRepository.count()).isZero()
    }

    @Test
    fun update_existingClient_updatesFields() {
        // Given
        val existing = saveClient("Old", "Name")

        // When
        val result = clientService.update(updateRequest(existing.id).apply { firstName = "New" })

        // Then
        val response = (result as ClientUpdateResult.Success).response
        assertThat(response.firstName).isEqualTo("New")
        assertThat(clientRepository.findById(existing.id).get().firstName).isEqualTo("New")
    }

    @Test
    fun update_unknownClient_returnsNotFound() {
        assertThat(clientService.update(updateRequest(9999))).isEqualTo(ClientUpdateResult.NotFound)
    }

    @Test
    fun update_archivedClient_returnsArchivedAndKeepsData() {
        // Given
        val existing = saveClient("Old", "Name", archived = true)

        // When
        val result = clientService.update(updateRequest(existing.id).apply { firstName = "New" })

        // Then
        assertThat(result).isEqualTo(ClientUpdateResult.Archived)
        assertThat(clientRepository.findById(existing.id).get().firstName).isEqualTo("Old")
    }

    @Test
    fun update_unknownInstitution_returnsInstitutionNotFoundAndKeepsData() {
        // Given
        val existing = saveClient("Old", "Name")

        // When
        val result = clientService.update(updateRequest(existing.id).apply {
            firstName = "New"
            institutionId = 9999
        })

        // Then
        assertThat(result).isEqualTo(ClientUpdateResult.InstitutionNotFound)
        assertThat(clientRepository.findById(existing.id).get().firstName).isEqualTo("Old")
    }

    @Test
    fun deleteById_removesTheClient() {
        // Given
        val existing = saveClient("Max", "Mustermann")

        // When
        clientService.deleteById(existing.id)

        // Then
        assertThat(clientRepository.findById(existing.id)).isEmpty
    }

    @Test
    fun getById_mapsCategoryTemplateIdAndTitle() {
        // Given
        val client = saveClient("Max", "Mustermann")

        // When
        val result = clientService.getById(client.id, includeArchived = false, leadingInstitutionIds = emptyList())

        // Then
        assertThat(result!!.categoryTemplateId).isEqualTo(categoryTemplate.id)
        assertThat(result.categoryTemplateTitle).isEqualTo("Template")
        assertThat(result.institution.id).isEqualTo(institution.id)
    }

    @Test
    fun getById_archivedClient_isHiddenUnlessIncludedOrLeading() {
        // Given
        val archived = saveClient("Archived", "Client", archived = true)

        // When
        val hidden = clientService.getById(archived.id, includeArchived = false, leadingInstitutionIds = emptyList())
        val included = clientService.getById(archived.id, includeArchived = true, leadingInstitutionIds = emptyList())
        val leading = clientService.getById(archived.id, includeArchived = false, leadingInstitutionIds = listOf(institution.id!!))

        // Then
        assertThat(hidden).isNull()
        assertThat(included!!.archived).isTrue()
        assertThat(leading).isNotNull
    }

    @Test
    fun getAllClientsWithInstitution_filtersArchivedClientsAndSortsByLastName() {
        // Given
        saveClient("Zara", "zimmer")
        saveClient("Anna", "Becker")
        saveClient("Archived", "Client", archived = true)

        // When
        val hidden = clientService.getAllClientsWithInstitution(false, emptyList())
        val all = clientService.getAllClientsWithInstitution(true, emptyList())

        // Then
        assertThat(hidden.map { it.lastName }).containsExactly("Becker", "zimmer")
        assertThat(all).hasSize(3)
        assertThat(hidden.first().institution.name).isEqualTo("Inst")
    }

    @Test
    fun getAllClientsWithInstitution_loadsTheInstitutionsWithAConstantNumberOfQueries() {
        // Given
        repeat(3) { saveClientOfNewInstitution(it) }
        entityManager.flush()
        entityManager.clear()
        val queryCounter = QueryCounter(entityManagerFactory)
        val queriesForFewClients = queryCounter.count { clientService.getAllClientsWithInstitution(true, emptyList()) }

        repeat(17) { saveClientOfNewInstitution(it + 3) }
        entityManager.flush()
        entityManager.clear()

        // When
        val queriesForManyClients = queryCounter.count { clientService.getAllClientsWithInstitution(true, emptyList()) }

        // Then
        assertThat(queriesForManyClients).isEqualTo(queriesForFewClients)
    }

    @Test
    fun getAllClientNames_includesArchivedClients() {
        // Given
        saveClient("Max", "Mustermann")
        saveClient("Archived", "Client", archived = true)

        // When
        val result = clientService.getAllClientNameDtos()

        // Then
        assertThat(result.map { it.lastName }).containsExactlyInAnyOrder("Mustermann", "Client")
    }

    @Test
    fun existsById_reflectsStoredClients() {
        // Given
        val client = saveClient("Max", "Mustermann")

        // When / Then
        assertThat(clientService.existsById(client.id)).isTrue()
        assertThat(clientService.existsById(9999)).isFalse()
    }

    private fun saveClient(firstName: String, lastName: String, archived: Boolean = false): Client =
        clientRepository.save(
            Client(
                firstName = firstName,
                lastName = lastName,
                institution = institution,
                categoryTemplate = categoryTemplate,
                archived = archived
            )
        )

    private fun createRequest() = ClientCreateRequest().apply {
        firstName = "Max"
        lastName = "Mustermann"
        institutionId = institution.id!!
        categoryTemplateId = categoryTemplate.id
    }

    private fun updateRequest(id: Long) = ClientUpdateRequest().apply {
        this.id = id
        firstName = "Max"
        lastName = "Mustermann"
        institutionId = institution.id!!
        categoryTemplateId = categoryTemplate.id
    }

    private fun saveClientOfNewInstitution(index: Int): Client {
        val ownInstitution = institutionRepository.save(
            Institution(name = "Inst $index", email = "a@b.c", phonenumber = "1")
        )
        return clientRepository.save(
            Client(firstName = "Max", lastName = "Client $index", institution = ownInstitution, categoryTemplate = categoryTemplateRepository.findById(categoryTemplate.id).get())
        )
    }
}
