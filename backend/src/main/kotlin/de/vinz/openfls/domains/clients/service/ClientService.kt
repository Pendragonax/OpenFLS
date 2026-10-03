package de.vinz.openfls.domains.clients.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.categories.service.CategoryTemplateService
import de.vinz.openfls.domains.clients.dto.ClientCreateRequest
import de.vinz.openfls.domains.clients.dto.ClientCreateResult
import de.vinz.openfls.domains.clients.dto.ClientDetailResponse
import de.vinz.openfls.domains.clients.dto.ClientNameDto
import de.vinz.openfls.domains.clients.dto.ClientUpdateRequest
import de.vinz.openfls.domains.clients.dto.ClientUpdateResult
import de.vinz.openfls.domains.clients.dto.ClientWithInstitutionResponse
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.clients.repository.ClientRepository
import de.vinz.openfls.domains.institutions.service.InstitutionService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ClientService(
    private val clientRepository: ClientRepository,
    private val institutionService: InstitutionService,
    private val categoryTemplateService: CategoryTemplateService
) {

    @Transactional
    fun create(request: ClientCreateRequest): ClientCreateResult {
        val institution = institutionService.getEntityById(request.institutionId)
            ?: return ClientCreateResult.InstitutionNotFound
        val categoryTemplate = categoryTemplateService.getEntityById(request.categoryTemplateId)
            ?: return ClientCreateResult.CategoryTemplateNotFound

        val client = clientRepository.save(
            Client(
                firstName = request.firstName,
                lastName = request.lastName,
                phoneNumber = request.phoneNumber,
                email = request.email,
                archived = false,
                institution = institution,
                categoryTemplate = categoryTemplate
            )
        )

        return ClientCreateResult.Success(ClientDetailResponse.from(client))
    }

    @Transactional
    fun update(request: ClientUpdateRequest): ClientUpdateResult {
        val client = clientRepository.findByIdOrNull(request.id) ?: return ClientUpdateResult.NotFound
        if (client.archived) {
            return ClientUpdateResult.Archived
        }
        val institution = institutionService.getEntityById(request.institutionId)
            ?: return ClientUpdateResult.InstitutionNotFound
        val categoryTemplate = categoryTemplateService.getEntityById(request.categoryTemplateId)
            ?: return ClientUpdateResult.CategoryTemplateNotFound

        client.firstName = request.firstName
        client.lastName = request.lastName
        client.phoneNumber = request.phoneNumber
        client.email = request.email
        client.institution = institution
        client.categoryTemplate = categoryTemplate

        return ClientUpdateResult.Success(ClientDetailResponse.from(clientRepository.save(client)))
    }

    @Transactional
    fun deleteById(id: Long) {
        clientRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAllClientsWithInstitution(
        includeArchived: Boolean,
        leadingInstitutionIds: List<Long>
    ): List<ClientWithInstitutionResponse> {
        return clientRepository.findAll()
            .filter { isVisible(it, includeArchived, leadingInstitutionIds) }
            .map { ClientWithInstitutionResponse.from(it) }
            .sortedBy { it.lastName.lowercase() }
    }

    @Transactional(readOnly = true)
    fun getAllClientNameDtos(): List<ClientNameDto> {
        return clientRepository.findAll().map { ClientNameDto.from(it) }
    }

    @Transactional(readOnly = true)
    fun getById(
        id: Long,
        includeArchived: Boolean,
        leadingInstitutionIds: List<Long>
    ): ClientDetailResponse? {
        val client = clientRepository.findByIdOrNull(id) ?: return null

        return if (isVisible(client, includeArchived, leadingInstitutionIds)) ClientDetailResponse.from(client) else null
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return clientRepository.existsById(id)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): Client? {
        return clientRepository.findByIdOrNull(id)
    }

    @InternalEntityApi
    @Transactional
    fun saveEntity(client: Client): Client {
        return clientRepository.save(client)
    }

    private fun isVisible(client: Client, includeArchived: Boolean, leadingInstitutionIds: List<Long>): Boolean {
        return !client.archived || includeArchived || leadingInstitutionIds.contains(client.institution?.id ?: 0)
    }
}
