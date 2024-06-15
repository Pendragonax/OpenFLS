package de.vinz.openfls.domains.hourTypes

import de.vinz.openfls.domains.hourTypes.dtos.HourTypeDto
import de.vinz.openfls.services.GenericService
import org.springframework.stereotype.Service
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper

@Service
class HourTypeService(
    private val hourTypeRepository: HourTypeRepository,
    private val modelMapper: ModelMapper
): GenericService<HourType> {

    @Transactional
    fun create(value: HourTypeDto): HourTypeDto {
        val entity = create(modelMapper.map(value, HourType::class.java))
        return modelMapper.map(entity, HourTypeDto::class.java)
    }

    @Transactional
    override fun create(value: HourType): HourType {
        return hourTypeRepository.save(value)
    }

    @Transactional
    fun update(hourTypeDto: HourTypeDto): HourTypeDto {
        if (!hourTypeRepository.existsById(hourTypeDto.id))
            throw IllegalArgumentException("Type of hour with id ${hourTypeDto.id} does not exists.")

        val entity = hourTypeRepository.save(modelMapper.map(hourTypeDto, HourType::class.java))

        return modelMapper.map(entity, HourTypeDto::class.java);
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

    fun getAllDtos(): List<HourTypeDto> {
        val entities = getAll()
                .sortedBy { it.title.lowercase() }
        return entities.map { modelMapper.map(it, HourTypeDto::class.java) }
    }

    override fun getAll(): List<HourType> {
        return hourTypeRepository.findAll().toList()
    }

    fun getDtoById(id: Long): HourTypeDto? {
        return modelMapper.map(hourTypeRepository.findById(id).orElse(null), HourTypeDto::class.java)
    }

    override fun getById(id: Long): HourType? {
        return hourTypeRepository.findById(id).orElse(null)
    }

    override fun existsById(id: Long): Boolean {
        return hourTypeRepository.existsById(id)
    }
}