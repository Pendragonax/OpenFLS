package de.vinz.openfls.domains.employees.services

import de.vinz.openfls.domains.employees.dtos.UnprofessionalDto
import de.vinz.openfls.domains.employees.entities.Unprofessional
import de.vinz.openfls.domains.employees.UnprofessionalRepository
import de.vinz.openfls.services.GenericService
import org.springframework.transaction.annotation.Transactional
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service

@Service
class UnprofessionalService(
        private val unprofessionalRepository: UnprofessionalRepository,
        private val modelMapper: ModelMapper
): GenericService<Unprofessional> {

    @Transactional
    override fun create(value: Unprofessional): Unprofessional {
        return unprofessionalRepository.save(value)
    }

    @Transactional
    override fun update(value: Unprofessional): Unprofessional {
        return unprofessionalRepository.save(value)
    }

    @Transactional
    override fun delete(id: Long) {
        return unprofessionalRepository.deleteById(id)
    }

    @Transactional
    fun deleteByEmployeeIdSponsorId(employeeId: Long, sponsorId: Long) {
        return unprofessionalRepository.deleteByEmployeeIdSponsorId(employeeId, sponsorId)
    }

    @Transactional(readOnly = true)
    override fun getAll(): List<Unprofessional> {
        return unprofessionalRepository.findAll().toList()
    }

    @Transactional(readOnly = true)
    override fun getById(id: Long): Unprofessional? {
        return unprofessionalRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    override fun existsById(id: Long): Boolean {
        return unprofessionalRepository.existsById(id)
    }

    @Transactional(readOnly = true)
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
