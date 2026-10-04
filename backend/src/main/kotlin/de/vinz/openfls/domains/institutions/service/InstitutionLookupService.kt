package de.vinz.openfls.domains.institutions.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Loads institutions without depending on any other service. Employees and institutions refer to
 * each other through their permissions, so the permission code needs a way to resolve institutions
 * that does not run through [InstitutionService], which in turn needs the employee service.
 */
@Service
class InstitutionLookupService(
    private val institutionRepository: InstitutionRepository
) {

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): Institution? {
        return institutionRepository.findByIdOrNull(id)
    }
}
