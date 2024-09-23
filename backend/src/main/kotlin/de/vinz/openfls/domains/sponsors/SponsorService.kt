package de.vinz.openfls.domains.sponsors

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class SponsorService(private val sponsorRepository: SponsorRepository) {

    @Transactional
    fun create(sponsorDto: SponsorDto): SponsorDto {
        sponsorDto.unprofessionals = null
        val entity = sponsorRepository.save(Sponsor.from(sponsorDto))
        return SponsorDto.from(entity)
    }

    @Transactional
    fun update(sponsorDto: SponsorDto): SponsorDto {
        sponsorDto.unprofessionals = null
        val entity = sponsorRepository.save(Sponsor.from(sponsorDto))
        return SponsorDto.from(entity)
    }

    @Transactional
    fun delete(id: Long) {
        sponsorRepository.deleteById(id)
    }

    fun getAll(): List<SponsorDto> {
        val entities = sponsorRepository.findAll()
        return entities.map { SponsorDto.from(it) }
                .sortedBy { it.name.lowercase() }
    }

    fun getDtoById(id: Long): SponsorDto? {
        val entities = sponsorRepository.findById(id).orElse(null)
        return SponsorDto.from(entities)
    }

    fun getById(id: Long): Sponsor? {
        return sponsorRepository.findById(id).orElse(null)
    }

    fun existsById(id: Long): Boolean {
        return sponsorRepository.existsById(id)
    }
}