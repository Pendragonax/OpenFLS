package de.vinz.openfls.domains.sponsors

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.sponsors.dtos.SponsorCreateRequest
import de.vinz.openfls.domains.sponsors.dtos.SponsorResponse
import de.vinz.openfls.domains.sponsors.dtos.SponsorUpdateRequest
import de.vinz.openfls.domains.sponsors.dtos.SponsorWithUnprofessionalsResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SponsorService(private val sponsorRepository: SponsorRepository) {

    @Transactional
    fun create(request: SponsorCreateRequest): SponsorResponse {
        val entity = sponsorRepository.save(
            Sponsor(name = request.name, payOverhang = request.payOverhang, payExact = request.payExact)
        )
        return SponsorResponse.from(entity)
    }

    @Transactional
    fun update(request: SponsorUpdateRequest): SponsorResponse {
        val entity = sponsorRepository.findById(request.id)
            .orElseThrow { IllegalArgumentException("sponsor with id ${request.id} not found") }
        entity.name = request.name
        entity.payOverhang = request.payOverhang
        entity.payExact = request.payExact
        return SponsorResponse.from(sponsorRepository.save(entity))
    }

    @Transactional
    fun delete(id: Long) {
        sponsorRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<SponsorResponse> {
        return sponsorRepository.findAll()
            .map { SponsorResponse.from(it) }
            .sortedBy { it.name.lowercase() }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): SponsorWithUnprofessionalsResponse? {
        return sponsorRepository.findById(id).orElse(null)?.let { SponsorWithUnprofessionalsResponse.from(it) }
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): Sponsor? {
        return sponsorRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return sponsorRepository.existsById(id)
    }
}
