package de.vinz.openfls.domains.institutions

import de.vinz.openfls.domains.institutions.Institution
import org.springframework.data.repository.CrudRepository

interface InstitutionRepository : CrudRepository<Institution, Long>