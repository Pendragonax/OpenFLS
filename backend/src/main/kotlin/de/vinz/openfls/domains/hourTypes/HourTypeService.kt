package de.vinz.openfls.domains.hourTypes

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeCreateRequest
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeResponse
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HourTypeService(private val hourTypeRepository: HourTypeRepository) {

    @Transactional
    fun create(request: HourTypeCreateRequest): HourTypeResponse {
        val entity = hourTypeRepository.save(HourType(title = request.title, price = request.price))
        return HourTypeResponse.from(entity)
    }

    @Transactional
    fun update(request: HourTypeUpdateRequest): HourTypeResponse {
        val entity = hourTypeRepository.findById(request.id)
            .orElseThrow { IllegalArgumentException("hour type with id ${request.id} not found") }
        entity.title = request.title
        entity.price = request.price
        return HourTypeResponse.from(hourTypeRepository.save(entity))
    }

    @Transactional
    fun delete(id: Long) {
        hourTypeRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<HourTypeResponse> {
        return hourTypeRepository.findAll()
            .sortedBy { it.title.lowercase() }
            .map { HourTypeResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): HourTypeResponse? {
        return hourTypeRepository.findById(id).orElse(null)?.let { HourTypeResponse.from(it) }
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): HourType? {
        return hourTypeRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return hourTypeRepository.existsById(id)
    }
}
