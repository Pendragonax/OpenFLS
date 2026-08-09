package de.vinz.openfls.domains.hourCorridors

import de.vinz.openfls.domains.hourCorridors.dtos.CreateHourCorridorDto
import de.vinz.openfls.domains.hourCorridors.dtos.HourCorridorDto
import de.vinz.openfls.domains.hourCorridors.dtos.UpdateHourCorridorDto
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.hourTypes.HourTypeService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HourCorridorService(
    private val hourCorridorRepository: HourCorridorRepository,
    private val assistancePlanRepository: AssistancePlanRepository,
    private val hourTypeService: HourTypeService
) {

    @Transactional
    fun create(hourCorridorDto: CreateHourCorridorDto): HourCorridorDto {
        validateRange(hourCorridorDto.weeklyMinutesFrom, hourCorridorDto.weeklyMinutesTill)

        val hourType = hourTypeService.getById(hourCorridorDto.hourTypeId)
            ?: throw IllegalArgumentException("hour type with id ${hourCorridorDto.hourTypeId} not found")

        val entity = hourCorridorRepository.save(
            HourCorridor(
                title = hourCorridorDto.title,
                weeklyMinutesFrom = hourCorridorDto.weeklyMinutesFrom,
                weeklyMinutesTill = hourCorridorDto.weeklyMinutesTill,
                hourType = hourType
            )
        )
        return HourCorridorDto.from(entity, countByAssistancePlan(entity.id))
    }

    @Transactional
    fun update(hourCorridorDto: UpdateHourCorridorDto): HourCorridorDto {
        if (!existsById(hourCorridorDto.id)) {
            throw IllegalArgumentException("hour corridor not found")
        }

        validateRange(hourCorridorDto.weeklyMinutesFrom, hourCorridorDto.weeklyMinutesTill)

        val hourType = hourTypeService.getById(hourCorridorDto.hourTypeId)
            ?: throw IllegalArgumentException("hour type with id ${hourCorridorDto.hourTypeId} not found")

        val entity = hourCorridorRepository.save(
            HourCorridor(
                id = hourCorridorDto.id,
                title = hourCorridorDto.title,
                weeklyMinutesFrom = hourCorridorDto.weeklyMinutesFrom,
                weeklyMinutesTill = hourCorridorDto.weeklyMinutesTill,
                hourType = hourType
            )
        )
        return HourCorridorDto.from(entity, countByAssistancePlan(entity.id))
    }

    @Transactional
    fun delete(id: Long) {
        val usageCount = countByAssistancePlan(id)
        if (usageCount > 0) {
            throw IllegalArgumentException("hour corridor is used by $usageCount assistance plans")
        }

        hourCorridorRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<HourCorridorDto> {
        val entities = hourCorridorRepository.findAll()
            .toList()
            .sortedBy { it.title.lowercase() }
        val assistancePlanCounts = countByAssistancePlanIds(entities.map { it.id })
        return entities.map { HourCorridorDto.from(it, assistancePlanCounts[it.id] ?: 0L) }
    }

    @Transactional(readOnly = true)
    fun getDtoById(id: Long): HourCorridorDto? {
        val entity = hourCorridorRepository.findById(id).orElse(null)
        return entity?.let { HourCorridorDto.from(it, countByAssistancePlan(it.id)) }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): HourCorridor? {
        return hourCorridorRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return hourCorridorRepository.existsById(id)
    }

    @Transactional(readOnly = true)
    fun countByAssistancePlan(id: Long): Long {
        return assistancePlanRepository.countByHourCorridorId(id)
    }

    private fun countByAssistancePlanIds(ids: List<Long>): Map<Long, Long> {
        if (ids.isEmpty()) {
            return emptyMap()
        }
        return assistancePlanRepository.countByHourCorridorIds(ids)
            .associate { it.hourCorridorId to it.assistancePlanCount }
    }

    private fun validateRange(weeklyMinutesFrom: Int, weeklyMinutesTill: Int) {
        if (weeklyMinutesTill < weeklyMinutesFrom) {
            throw IllegalArgumentException("till before from")
        }
    }
}
