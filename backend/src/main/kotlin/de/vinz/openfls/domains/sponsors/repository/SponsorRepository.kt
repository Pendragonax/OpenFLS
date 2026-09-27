package de.vinz.openfls.domains.sponsors.repository

import de.vinz.openfls.domains.sponsors.entity.Sponsor

import org.springframework.data.repository.CrudRepository

interface SponsorRepository : CrudRepository<Sponsor, Long> {
}