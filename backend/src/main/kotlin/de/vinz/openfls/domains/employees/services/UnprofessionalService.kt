package de.vinz.openfls.domains.employees.services

import de.vinz.openfls.domains.employees.dtos.UnprofessionalDto
import de.vinz.openfls.domains.employees.entities.Unprofessional
import de.vinz.openfls.domains.employees.UnprofessionalRepository
import de.vinz.openfls.services.GenericService
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service

@Service
class UnprofessionalService(
        private val unprofessionalRepository: UnprofessionalRepository,
        private val modelMapper: ModelMapper
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

    fun convertToUnprofessionals(dtos: Array<UnprofessionalDto>?, employeeId: Long): MutableSet<Unprofessional> {
        return dtos
                ?.map {
                    modelMapper
                            .map(it, Unprofessional::class.java)
                            .apply { it.employeeId = employeeId } }
                ?.toMutableSet() ?: mutableSetOf()
    }
}