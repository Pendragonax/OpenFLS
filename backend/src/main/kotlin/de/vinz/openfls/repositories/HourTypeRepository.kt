package de.vinz.openfls.repositories

import de.vinz.openfls.entities.HourType
import org.springframework.data.repository.CrudRepository

interface HourTypeRepository : CrudRepository<HourType, Long>