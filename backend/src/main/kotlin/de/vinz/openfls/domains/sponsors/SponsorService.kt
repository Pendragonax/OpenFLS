package de.vinz.openfls.domains.sponsors

import de.vinz.openfls.services.GenericService
import org.springframework.stereotype.Service
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper

@Service
class SponsorService(
        private val sponsorRepository: SponsorRepository,
        private val modelMapper: ModelMapper
) : GenericService<Sponsor> {

    @Transactional
    fun create(sponsorDto: SponsorDto): SponsorDto {
        val entity = create(modelMapper.map(sponsorDto, Sponsor::class.java))
        return modelMapper.map(entity, SponsorDto::class.java)
    }

    @Transactional
    override fun create(value: Sponsor): Sponsor {
        value.unprofessionals = null

        return sponsorRepository.save(value)
    }

    @Transactional
    fun update(sponsorDto: SponsorDto): SponsorDto {
        val entity = update(modelMapper.map(sponsorDto, Sponsor::class.java))
        return modelMapper.map(entity, SponsorDto::class.java)
    }

    @Transactional
    override fun update(value: Sponsor): Sponsor {
        value.unprofessionals = null

        return sponsorRepository.save(value)
    }

    @Transactional
    override fun delete(id: Long) {
        sponsorRepository.deleteById(id)
    }

    fun getAllDtos(): List<SponsorDto> {
        return getAll()
                .map { modelMapper.map(it, SponsorDto::class.java) }
                .sortedBy { it.name.lowercase() }
    }

    override fun getAll(): List<Sponsor> {
        return sponsorRepository.findAll().toList()
    }

    fun getDtoById(id: Long): SponsorDto? {
        return modelMapper.map(sponsorRepository.findById(id).orElse(null), SponsorDto::class.java)
    }

    override fun getById(id: Long): Sponsor? {
        return sponsorRepository.findById(id).orElse(null)
    }

    override fun existsById(id: Long): Boolean {
        return sponsorRepository.existsById(id)
    }
}