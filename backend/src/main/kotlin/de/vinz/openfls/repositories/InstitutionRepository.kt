package de.vinz.openfls.repositories

import de.vinz.openfls.model.Institution
import org.springframework.data.repository.CrudRepository

interface InstitutionRepository : CrudRepository<Institution, Long>