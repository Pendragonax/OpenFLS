package de.vinz.openfls.domains.institutions

import de.vinz.openfls.domains.institutions.projections.InstitutionSoloProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface InstitutionRepository : CrudRepository<Institution, Long> {

    @Query("SELECT u FROM Institution u ORDER BY u.name")
    fun findInstitutionSoloProjectionOrderedByName(): List<InstitutionSoloProjection>
}