package de.vinz.openfls.domains.sponsors

import org.springframework.data.repository.CrudRepository

interface SponsorRepository : CrudRepository<Sponsor, Long> {
}