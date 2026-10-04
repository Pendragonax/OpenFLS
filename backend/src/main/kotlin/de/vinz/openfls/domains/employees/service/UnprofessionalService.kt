package de.vinz.openfls.domains.employees.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.employees.dto.UnprofessionalRequest
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.entity.Unprofessional
import de.vinz.openfls.domains.employees.entity.UnprofessionalKey
import de.vinz.openfls.domains.employees.repository.UnprofessionalRepository
import de.vinz.openfls.domains.sponsors.service.SponsorService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UnprofessionalService(
        private val unprofessionalRepository: UnprofessionalRepository,
        private val sponsorService: SponsorService
) {

    @InternalEntityApi
    @Transactional
    fun createEntity(value: Unprofessional): Unprofessional {
        return unprofessionalRepository.save(value)
    }

    @Transactional
    fun deleteByEmployeeIdAndSponsorId(employeeId: Long, sponsorId: Long) {
        unprofessionalRepository.deleteByEmployeeIdAndSponsorId(employeeId, sponsorId)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByEmployeeId(employeeId: Long): List<Unprofessional> {
        return unprofessionalRepository.findByEmployeeId(employeeId)
    }

    /**
     * Builds the entities for the given requests. Returns `null` if one of the sponsors does not exist.
     */
    @InternalEntityApi
    @Transactional(readOnly = true)
    fun buildEntitiesFromRequests(requests: List<UnprofessionalRequest>, employee: Employee): MutableSet<Unprofessional>? {
        val unprofessionals = mutableSetOf<Unprofessional>()
        for (request in requests) {
            val sponsor = sponsorService.getEntityById(request.sponsorId) ?: return null
            unprofessionals.add(
                Unprofessional(
                    id = UnprofessionalKey(employeeId = employee.id, sponsorId = sponsor.id),
                    employee = employee,
                    sponsor = sponsor,
                    end = request.end
                )
            )
        }

        return unprofessionals
    }
}
