package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourResponseDto
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanHourRepository
import de.vinz.openfls.domains.hourTypes.service.HourTypeService
import org.springframework.data.repository.findByIdOrNull
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
    fun save(assistancePlanHour: AssistancePlanHourDto): AssistancePlanHourResponseDto {
        val mappedEntity = modelMapper.map(assistancePlanHour, AssistancePlanHour::class.java)
        mappedEntity.assistancePlan = assistancePlanService.getEntityById(mappedEntity.assistancePlan?.id ?: 0)
        mappedEntity.hourType = hourTypeService.getEntityById(mappedEntity.hourType?.id ?: 0)

        val savedEntity = assistancePlanHourRepository.save(mappedEntity)
        return toResponseDto(savedEntity)
    }

    @Transactional
    fun delete(id: Long) {
        return assistancePlanHourRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): AssistancePlanHourResponseDto? {
        return assistancePlanHourRepository.findByIdOrNull(id)?.let(::toResponseDto)
    }

    private fun toResponseDto(entity: AssistancePlanHour): AssistancePlanHourResponseDto {
        return AssistancePlanHourResponseDto().apply {
            id = entity.id
            weeklyMinutes = entity.weeklyMinutes
            assistancePlanId = entity.assistancePlan?.id ?: 0
            hourTypeId = entity.hourType?.id ?: 0
            hourTypeTitle = entity.hourType?.title ?: ""
        }
    }
}
