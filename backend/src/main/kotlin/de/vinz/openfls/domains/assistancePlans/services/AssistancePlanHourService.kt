package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourDto
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanHourProjection
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanHourSoloProjection
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanHourRepository
import de.vinz.openfls.domains.hourTypes.HourTypeService
import org.springframework.transaction.annotation.Transactional
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service

@Service
class AssistancePlanHourService(
        private val assistancePlanHourRepository: AssistancePlanHourRepository,
        private val assistancePlanService: AssistancePlanService,
        private val hourTypeService: HourTypeService,
        private val modelMapper: ModelMapper) {

    @Transactional
    fun save(assistancePlanHour: AssistancePlanHourDto): AssistancePlanHourProjection {
        val mappedEntity = modelMapper.map(assistancePlanHour, AssistancePlanHour::class.java)
        mappedEntity.assistancePlan = assistancePlanService.getById(mappedEntity.assistancePlan?.id ?: 0)
        mappedEntity.hourType = hourTypeService.getById(mappedEntity.hourType?.id ?: 0)

        val savedEntity = assistancePlanHourRepository.save(mappedEntity)
        return AssistancePlanHourProjection.from(savedEntity)
    }

    @Transactional
    fun delete(id: Long) {
        return assistancePlanHourRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): AssistancePlanHourSoloProjection {
        return assistancePlanHourRepository.findProjectionById(id)
    }
}
