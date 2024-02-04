package de.vinz.openfls.services

import de.vinz.openfls.dtos.ClientInstitutionDto
import de.vinz.openfls.dtos.ClientSimpleDto
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.entities.Client
import de.vinz.openfls.repositories.ClientRepository
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import javax.transaction.Transactional

@Service
class ClientService(
    private val clientRepository: ClientRepository,
    private val institutionService: InstitutionService,
    private val categoryTemplateService: CategoryTemplateService,
    private val modelMapper: ModelMapper
): GenericService<Client> {

    @Transactional
    override fun create(value: Client): Client {
        value.institution = institutionService.getById(value.institution.id ?: 0)
            ?: throw IllegalArgumentException("institution not found")
        value.categoryTemplate = categoryTemplateService.getById(value.categoryTemplate.id ?: 0)
            ?: throw IllegalArgumentException("category template not found")

        return clientRepository.save(value)
    }

    @Transactional
    override fun update(value: Client): Client {
        if (!clientRepository.existsById(value.id))
            throw IllegalArgumentException("client not found")

        value.institution = institutionService.getById(value.institution.id ?: 0)
            ?: throw IllegalArgumentException("institution not found")
        value.categoryTemplate = categoryTemplateService.getById(value.categoryTemplate.id ?: 0)
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

    fun getAllSimple(): List<ClientSimpleDto> {
        val clientsInstitutions = clientRepository.findAllClientSimpleDto()
        return clientsInstitutions.map { modelMapper.map(it, ClientSimpleDto::class.java) }
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
}