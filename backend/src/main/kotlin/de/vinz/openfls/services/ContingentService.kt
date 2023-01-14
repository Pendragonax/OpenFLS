package de.vinz.openfls.services

import de.vinz.openfls.model.Contingent
import de.vinz.openfls.repositories.ContingentRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class ContingentService(
    private val contingentRepository: ContingentRepository
) : GenericService<Contingent> {

    override fun create(value: Contingent): Contingent {
        return contingentRepository.save(value)
    }

    override fun update(value: Contingent): Contingent {
        if (!contingentRepository.existsById(value.id ?: 0))
            throw IllegalArgumentException("id not found")

        return contingentRepository.save(value)
    }

    override fun delete(id: Long) {
        return contingentRepository.deleteById(id)
    }

    override fun getAll(): List<Contingent> {
        return contingentRepository.findAll().toList()
    }

    override fun getById(id: Long): Contingent? {
        return contingentRepository.findByIdOrNull(id)
    }

    override fun existsById(id: Long): Boolean {
        return contingentRepository.existsById(id)
    }

    fun getByEmployeeId(id: Long): List<Contingent> {
        return contingentRepository.findAllByEmployeeId(id)
    }

    fun getByInstitutionId(id: Long): List<Contingent> {
        return contingentRepository.findAllByInstitutionId(id)
    }
}