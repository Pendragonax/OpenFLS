package de.vinz.openfls.services

import de.vinz.openfls.model.HourType
import de.vinz.openfls.repositories.HourTypeRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class HourTypeService(
    private val hourTypeRepository: HourTypeRepository
): GenericService<HourType> {

    @Transactional
    override fun create(value: HourType): HourType {
        return hourTypeRepository.save(value)
    }

    @Transactional
    override fun update(value: HourType): HourType {
        if (!hourTypeRepository.existsById(value.id))
            throw IllegalArgumentException("Type of hour with id ${value.id} does not exists.")

        return hourTypeRepository.save(value)
    }

    @Transactional
    override fun delete(id: Long) {
        if (!hourTypeRepository.existsById(id))
            throw IllegalArgumentException("Type of hour with id $id does not exists.")

        hourTypeRepository.deleteById(id)
    }

    override fun getAll(): List<HourType> {
        return hourTypeRepository.findAll().toList()
    }

    override fun getById(id: Long): HourType? {
        return hourTypeRepository.findById(id).orElse(null)
    }

    override fun existsById(id: Long): Boolean {
        return hourTypeRepository.existsById(id)
    }
}