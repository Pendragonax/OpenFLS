package de.vinz.openfls.services

import de.vinz.openfls.model.Sponsor
import de.vinz.openfls.repositories.SponsorRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class SponsorService(
    private val sponsorRepository: SponsorRepository
): GenericService<Sponsor> {

    @Transactional
    override fun create(value: Sponsor): Sponsor {
        value.unprofessionals = null

        return sponsorRepository.save(value)
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

    override fun getAll(): List<Sponsor> {
        return sponsorRepository.findAll().toList()
    }

    override fun getById(id: Long): Sponsor? {
        return sponsorRepository.findById(id).orElse(null)
    }

    override fun existsById(id: Long): Boolean {
        return sponsorRepository.existsById(id)
    }
}