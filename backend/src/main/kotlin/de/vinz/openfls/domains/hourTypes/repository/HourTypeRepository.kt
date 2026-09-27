package de.vinz.openfls.domains.hourTypes.repository

import de.vinz.openfls.domains.hourTypes.entity.HourType
import org.springframework.data.repository.CrudRepository

interface HourTypeRepository : CrudRepository<HourType, Long>