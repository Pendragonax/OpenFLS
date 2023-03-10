package de.vinz.openfls.services

import de.vinz.openfls.model.Client
import de.vinz.openfls.repositories.ClientRepository
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import javax.transaction.Transactional

@Service
class ClientService(
    private val clientRepository: ClientRepository,
    private val institutionService: InstitutionService,
    private val categoryTemplateService: CategoryTemplateService
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