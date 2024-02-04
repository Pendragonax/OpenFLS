package de.vinz.openfls.repositories

import de.vinz.openfls.entities.Institution
import org.springframework.data.repository.CrudRepository

interface InstitutionRepository : CrudRepository<Institution, Long>