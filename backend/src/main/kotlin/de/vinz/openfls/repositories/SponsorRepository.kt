package de.vinz.openfls.repositories

import de.vinz.openfls.entities.Sponsor
import org.springframework.data.repository.CrudRepository

interface SponsorRepository : CrudRepository<Sponsor, Long> {
}