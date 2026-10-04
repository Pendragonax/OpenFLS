package de.vinz.openfls.domains.institutions.repository

import de.vinz.openfls.domains.institutions.entity.Institution
import org.springframework.data.repository.CrudRepository

interface InstitutionRepository : CrudRepository<Institution, Long>
