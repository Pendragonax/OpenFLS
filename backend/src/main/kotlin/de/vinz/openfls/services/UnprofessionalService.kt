package de.vinz.openfls.services

import de.vinz.openfls.model.Unprofessional
import de.vinz.openfls.repositories.UnprofessionalRepository
import org.springframework.stereotype.Service

@Service
class UnprofessionalService(
    private val unprofessionalRepository: UnprofessionalRepository
): GenericService<Unprofessional> {
    override fun create(value: Unprofessional): Unprofessional {
        return unprofessionalRepository.save(value)
    }

    override fun update(value: Unprofessional): Unprofessional {
        return unprofessionalRepository.save(value)
    }

    override fun delete(id: Long) {
        return unprofessionalRepository.deleteById(id)
    }

    fun deleteByEmployeeIdSponsorId(employeeId: Long, sponsorId: Long) {
        return unprofessionalRepository.deleteByEmployeeIdSponsorId(employeeId, sponsorId)
    }

    override fun getAll(): List<Unprofessional> {
        return unprofessionalRepository.findAll().toList()
    }

    override fun getById(id: Long): Unprofessional? {
        return unprofessionalRepository.findById(id).orElse(null)
    }

    override fun existsById(id: Long): Boolean {
        return unprofessionalRepository.existsById(id)
    }

    fun getByEmployeeId(id: Long): List<Unprofessional> {
        return unprofessionalRepository.findByEmployeeId(id)
    }
}