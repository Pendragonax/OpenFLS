package de.vinz.openfls.domains.hourTypes

import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

@Service
class HourTypeService(private val hourTypeRepository: HourTypeRepository) {

    @Transactional
    fun create(hourTypeDto: HourTypeDto): HourTypeDto {
        val entity = hourTypeRepository.save(HourType.from(hourTypeDto))
        return HourTypeDto.from(entity)
    }

    @Transactional
    fun update(hourTypeDto: HourTypeDto): HourTypeDto {
        val entity = hourTypeRepository.save(HourType.from(hourTypeDto))
        return HourTypeDto.from(entity)
    }

    @Transactional
    fun delete(id: Long) {
        hourTypeRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<HourTypeDto> {
        val entities = hourTypeRepository.findAll()
                .toList()
                .sortedBy { it.title.lowercase() }
        return entities.map { HourTypeDto.from(it) }
    }

    @Transactional(readOnly = true)
    fun getDtoById(id: Long): HourTypeDto? {
        val entity = hourTypeRepository.findById(id).orElse(null)
        return entity?.let { HourTypeDto.from(it) }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): HourType? {
        return hourTypeRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return hourTypeRepository.existsById(id)
    }
}
