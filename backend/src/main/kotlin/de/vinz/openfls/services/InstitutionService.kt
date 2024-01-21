package de.vinz.openfls.services

import de.vinz.openfls.entities.*
import de.vinz.openfls.repositories.InstitutionRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class InstitutionService(
    private val institutionRepository: InstitutionRepository,
    private val permissionServiceImpl: PermissionService
): GenericService<Institution> {

    @Transactional
    override fun create(value: Institution): Institution {
        return save(value)
    }

    @Transactional
    override fun update(value: Institution): Institution {
        return save(value)
    }

    @Transactional
    private fun save(institution: Institution): Institution {
        val tmpPermissions = institution.permissions
        institution.permissions = null

        val tmpInstitution = institutionRepository.save(institution)
        tmpInstitution.permissions = permissionServiceImpl
            .savePermissionsByInstitution(tmpPermissions ?: mutableSetOf(), tmpInstitution)

        return tmpInstitution
    }

    @Transactional
    override fun delete(id: Long) {
        institutionRepository.deleteById(id)
    }

    override fun getAll(): List<Institution> {
        return institutionRepository.findAll().toList()
    }

    override fun getById(id: Long): Institution? {
        return institutionRepository.findById(id).orElse(null)
    }

    override fun existsById(id: Long): Boolean {
        return institutionRepository.existsById(id)
    }
}