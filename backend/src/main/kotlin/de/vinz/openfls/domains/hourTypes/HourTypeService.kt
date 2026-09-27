package de.vinz.openfls.domains.hourTypes

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeCreateRequest
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeDeleteResult
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeResponse
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeUpdateRequest
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeUpdateResult
import org.springframework.data.repository.findByIdOrNull
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
    fun update(request: HourTypeUpdateRequest): HourTypeUpdateResult {
        val entity = hourTypeRepository.findByIdOrNull(request.id)
            ?: return HourTypeUpdateResult.NotFound
        entity.title = request.title
        entity.price = request.price
        return HourTypeUpdateResult.Success(HourTypeResponse.from(hourTypeRepository.save(entity)))
    }

    @Transactional
    fun delete(id: Long): HourTypeDeleteResult {
        val entity = hourTypeRepository.findByIdOrNull(id)
            ?: return HourTypeDeleteResult.NotFound
        hourTypeRepository.deleteById(id)
        return HourTypeDeleteResult.Success(HourTypeResponse.from(entity))
    }

    @Transactional(readOnly = true)
    fun getAll(): List<HourTypeResponse> {
        return hourTypeRepository.findAll()
            .sortedBy { it.title.lowercase() }
            .map { HourTypeResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): HourTypeResponse? {
        return hourTypeRepository.findByIdOrNull(id)?.let { HourTypeResponse.from(it) }
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): HourType? {
        return hourTypeRepository.findByIdOrNull(id)
    }
}
