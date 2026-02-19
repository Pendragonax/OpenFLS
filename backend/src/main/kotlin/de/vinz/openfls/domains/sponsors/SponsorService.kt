package de.vinz.openfls.domains.sponsors

import org.springframework.transaction.annotation.Transactional
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

    @Transactional(readOnly = true)
    fun getAll(): List<SponsorDto> {
        val entities = sponsorRepository.findAll()
        return entities.map { SponsorDto.from(it) }
                .sortedBy { it.name.lowercase() }
    }

    @Transactional(readOnly = true)
    fun getDtoById(id: Long): SponsorDto? {
        val entities = sponsorRepository.findById(id).orElse(null)
        return SponsorDto.from(entities)
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): Sponsor? {
        return sponsorRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return sponsorRepository.existsById(id)
    }
}
