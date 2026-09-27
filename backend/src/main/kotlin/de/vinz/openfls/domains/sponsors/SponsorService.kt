package de.vinz.openfls.domains.sponsors

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.sponsors.dtos.SponsorCreateRequest
import de.vinz.openfls.domains.sponsors.dtos.SponsorDeleteResult
import de.vinz.openfls.domains.sponsors.dtos.SponsorResponse
import de.vinz.openfls.domains.sponsors.dtos.SponsorUpdateRequest
import de.vinz.openfls.domains.sponsors.dtos.SponsorUpdateResult
import de.vinz.openfls.domains.sponsors.dtos.SponsorWithUnprofessionalsResponse
import org.springframework.data.repository.findByIdOrNull
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
    fun update(request: SponsorUpdateRequest): SponsorUpdateResult {
        val entity = sponsorRepository.findByIdOrNull(request.id)
            ?: return SponsorUpdateResult.NotFound
        entity.name = request.name
        entity.payOverhang = request.payOverhang
        entity.payExact = request.payExact
        return SponsorUpdateResult.Success(SponsorResponse.from(sponsorRepository.save(entity)))
    }

    @Transactional
    fun delete(id: Long): SponsorDeleteResult {
        val entity = sponsorRepository.findByIdOrNull(id)
            ?: return SponsorDeleteResult.NotFound
        sponsorRepository.deleteById(id)
        return SponsorDeleteResult.Success(SponsorWithUnprofessionalsResponse.from(entity))
    }

    @Transactional(readOnly = true)
    fun getAll(): List<SponsorResponse> {
        return sponsorRepository.findAll()
            .map { SponsorResponse.from(it) }
            .sortedBy { it.name.lowercase() }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): SponsorWithUnprofessionalsResponse? {
        return sponsorRepository.findByIdOrNull(id)?.let { SponsorWithUnprofessionalsResponse.from(it) }
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): Sponsor? {
        return sponsorRepository.findByIdOrNull(id)
    }
}
