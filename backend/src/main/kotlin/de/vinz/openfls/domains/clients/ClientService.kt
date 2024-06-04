package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.dtos.ClientDto
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto
import de.vinz.openfls.services.CategoryTemplateService
import de.vinz.openfls.services.GenericService
import de.vinz.openfls.services.InstitutionService
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service

@Service
class ClientService(
        private val clientRepository: ClientRepository,
        private val institutionService: InstitutionService,
        private val categoryTemplateService: CategoryTemplateService,
        private val modelMapper: ModelMapper
) : GenericService<Client> {

    @Transactional
    fun create(value: ClientDto): ClientDto {
        val clientEntity = modelMapper.map(value, Client::class.java)
        val resultClientEntity = create(clientEntity)
        val clientDto = modelMapper.map(resultClientEntity, ClientDto::class.java)
        return sortClientDto(clientDto)
    }

    @Transactional
    override fun create(value: Client): Client {
        value.institution = institutionService.getById(value.institution?.id ?: 0)
                ?: throw IllegalArgumentException("institution not found")
        value.categoryTemplate = categoryTemplateService.getById(value.categoryTemplate?.id ?: 0)
                ?: throw IllegalArgumentException("category template not found")

        return clientRepository.save(value)
    }

    @Transactional
    fun update(value: ClientDto): ClientDto {
        val clientEntity = modelMapper.map(value, Client::class.java)
        val resultClientEntity = update(clientEntity)
        val clientDto = modelMapper.map(resultClientEntity, ClientDto::class.java)
        return sortClientDto(clientDto)
    }

    @Transactional
    override fun update(value: Client): Client {
        if (!clientRepository.existsById(value.id))
            throw IllegalArgumentException("client not found")

        value.institution = institutionService.getById(value.institution?.id ?: 0)
                ?: throw IllegalArgumentException("institution not found")
        value.categoryTemplate = categoryTemplateService.getById(value.categoryTemplate?.id ?: 0)
                ?: throw IllegalArgumentException("category template not found")

        return clientRepository.save(value)
    }

    @Transactional
    override fun delete(id: Long) {
        clientRepository.deleteById(id)
    }

    override fun getAll(): List<Client> {
        return clientRepository.findAll().toList()
    }

    fun getAllClientSimpleDto(): List<ClientSimpleDto> {
        val clientInstitutionDtos = clientRepository.findAllClientSimpleDto()
        return clientInstitutionDtos
                .map { modelMapper.map(it, ClientSimpleDto::class.java) }
                .sortedBy { it.lastName.lowercase() }
    }

    fun getDtoById(id: Long): ClientDto? {
        val entity = getById(id)

        if (entity != null) {
            val clientDto = modelMapper.map(entity, ClientDto::class.java)
            return sortClientDto(clientDto)
        }

        return null
    }

    override fun getById(id: Long): Client? {
        return clientRepository.findById(id).orElse(null)
    }

    override fun existsById(id: Long): Boolean {
        return clientRepository.existsById(id)
    }

    fun existById(id: Long): Boolean {
        return clientRepository.existsById(id)
    }

    private fun sortClientDto(clientDto: ClientDto): ClientDto {
        clientDto.assistancePlans =
                clientDto.assistancePlans.sortedBy { it.start }.toTypedArray()
        clientDto.categoryTemplate.categories =
                clientDto.categoryTemplate.categories.sortedBy { it.shortcut }.toTypedArray()
        return clientDto
    }
}