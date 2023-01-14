package de.vinz.openfls.repositories

import de.vinz.openfls.model.Sponsor
import org.springframework.data.repository.CrudRepository

interface SponsorRepository : CrudRepository<Sponsor, Long> {
}