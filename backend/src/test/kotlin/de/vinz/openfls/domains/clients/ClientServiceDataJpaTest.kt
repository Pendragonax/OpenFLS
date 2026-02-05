package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.categories.CategoryTemplateService
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
import de.vinz.openfls.domains.clients.dtos.ClientDto
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.domains.institutions.InstitutionService
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
}
