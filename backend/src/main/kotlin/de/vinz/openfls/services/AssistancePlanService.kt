package de.vinz.openfls.services

import de.vinz.openfls.dtos.ActualTargetValueDto
import de.vinz.openfls.dtos.AssistancePlanEvalDto
import de.vinz.openfls.dtos.HourTypeDto
import de.vinz.openfls.model.AssistancePlan
import de.vinz.openfls.repositories.AssistancePlanHourRepository
import de.vinz.openfls.repositories.AssistancePlanRepository
import de.vinz.openfls.repositories.ServiceRepository
import org.modelmapper.ModelMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import javax.transaction.Transactional
import kotlin.IllegalArgumentException

@Service
class AssistancePlanService(
    private val assistancePlanRepository: AssistancePlanRepository,
    private val assistancePlanHourRepository: AssistancePlanHourRepository,
    private val serviceRepository: ServiceRepository,
    private val modelMapper: ModelMapper
): GenericService<AssistancePlan> {
    @Transactional
    override fun create(value: AssistancePlan): AssistancePlan {
        if (value.id > 0)
            throw IllegalArgumentException("id is set")

        // backup hours
        val hours = value.hours
        value.goals = mutableSetOf()
        value.hours = mutableSetOf()

        // save
        val entity = assistancePlanRepository.save(value)

        // add hours
        entity.hours = hours
            .map { assistancePlanHourRepository
                .save(it.apply {
                    id = 0
                    assistancePlan = entity
                })}
            .toMutableSet()

        return entity
    }

    @Transactional
    override fun update(value: AssistancePlan): AssistancePlan {
        if (value.id <= 0)
            throw IllegalArgumentException("id is set")
        if (!assistancePlanRepository.existsById(value.id))
            throw IllegalArgumentException("id not found")

        // backup hours
        val hours = value.hours
        value.goals = mutableSetOf()
        value.hours = mutableSetOf()

        val entity = assistancePlanRepository.save(value)

        // delete goals
        assistancePlanHourRepository
            .findByAssistancePlanId(value.id)
            .filter { !hours.any { hour -> hour.id == it.id } }
            .forEach { assistancePlanHourRepository.deleteById(it.id) }

        // add / update goals
        entity.hours = hours
            .map { hour ->
                assistancePlanHourRepository.save(hour.apply {
                    assistancePlan = entity
                })}
            .toMutableSet()

        return entity
    }

    @Transactional
    override fun delete(id: Long) {
        return assistancePlanRepository.deleteById(id)
    }

    override fun getAll(): List<AssistancePlan> {
        return assistancePlanRepository.findAll().toList()
    }

    override fun getById(id: Long): AssistancePlan? {
        return assistancePlanRepository.findByIdOrNull(id)
    }

    override fun existsById(id: Long): Boolean {
        return assistancePlanRepository.existsById(id)
    }

    fun getByClientId(id: Long): List<AssistancePlan> {
        return assistancePlanRepository.findByClientId(id)
    }

    fun getBySponsorId(id: Long): List<AssistancePlan> {
        return assistancePlanRepository.findBySponsorId(id)
    }

    fun getByInstitutionId(id: Long): List<AssistancePlan> {
        return assistancePlanRepository.findByInstitutionId(id)
    }

    fun getEvaluationById(id: Long): AssistancePlanEvalDto {
        val assistancePlan = assistancePlanRepository.findById(id).orElseThrow { IllegalArgumentException("id not found ")}
        val services = serviceRepository.findByAssistancePlan(id)
        val eval = AssistancePlanEvalDto()

        val days = Duration.between(assistancePlan.start, assistancePlan.end).toDays()
        val daysTillToday = if (assistancePlan.end < LocalDate.now()) {
            Duration.between(assistancePlan.start, assistancePlan.end).toDays()
        } else {
            Duration.between(assistancePlan.start, LocalDate.now()).toDays()
        }

        eval.total = assistancePlan.hours.map {
            ActualTargetValueDto().apply {
                target = days * (it.weeklyHours / 7)
                hourType = modelMapper.map(it.hourType, HourTypeDto::class.java)
            }
        }

        eval.tillToday = assistancePlan.hours.map {
            ActualTargetValueDto().apply {
                target = daysTillToday * (it.weeklyHours / 7)
                hourType = modelMapper.map(it.hourType, HourTypeDto::class.java)
            }
        }

        for (service in services) {
            val tmp = eval.total.firstOrNull { it.hourType.id == service.hourType.id }

            if (tmp == null) {
                eval.notMatchingServices++
                eval.notMatchingServicesIds.add(service.id)
            } else {
                tmp.apply {
                    actual += service.minutes / 60.0
                    size++
                }
                eval.tillToday
                    .first { it.hourType.id == service.hourType.id }
                    .apply {
                        actual += service.minutes / 60.0
                        size++
                }
            }
        }

        return eval
    }
}