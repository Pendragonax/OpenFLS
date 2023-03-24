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
import java.time.LocalDate
import java.time.temporal.ChronoUnit
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

        val days = ChronoUnit.DAYS.between(assistancePlan.start, assistancePlan.end) + 1
        val tillDate = if (assistancePlan.end < LocalDate.now()) assistancePlan.end else LocalDate.now()
        val startActualYearDate =
            if (assistancePlan.start.year == LocalDate.now().year)
                assistancePlan.start
            else
                LocalDate.of(LocalDate.now().year, 1, 1)
        val startActualMonthDate =
            if (assistancePlan.start.year == LocalDate.now().year && assistancePlan.start.month == LocalDate.now().month)
                assistancePlan.start
            else
                LocalDate.of(LocalDate.now().year, LocalDate.now().month, 1)
        val daysTillToday = ChronoUnit.DAYS.between(assistancePlan.start, tillDate) + 1
        val daysTillTodayInActualYear = ChronoUnit.DAYS.between(startActualYearDate, tillDate) + 1
        val daysTillTodayInActualMonth = ChronoUnit.DAYS.between(startActualMonthDate, tillDate) + 1

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

        eval.actualYear = assistancePlan.hours.map {
            ActualTargetValueDto().apply {
                target = daysTillTodayInActualYear * (it.weeklyHours / 7)
                hourType = modelMapper.map(it.hourType, HourTypeDto::class.java)
            }
        }

        eval.actualMonth = assistancePlan.hours.map {
            ActualTargetValueDto().apply {
                target = daysTillTodayInActualMonth * (it.weeklyHours / 7)
                hourType = modelMapper.map(it.hourType, HourTypeDto::class.java)
            }
        }

        for (service in services) {
            val total = eval.total.firstOrNull { it.hourType.id == service.hourType.id &&
                    service.start.year <= assistancePlan.end.year &&
                    (service.start.month < assistancePlan.end.month ||
                            (service.start.month == assistancePlan.end.month && service.start.dayOfMonth <= assistancePlan.end.dayOfMonth))}

            // service is not between the start and end of the assistance plan
            if (total == null) {
                eval.notMatchingServices++
                eval.notMatchingServicesIds.add(service.id)
            } else {
                total.apply {
                    actual += service.minutes / 60.0
                    size++
                }

                eval.tillToday
                    .firstOrNull { it.hourType.id == service.hourType.id &&
                            service.start.year <= tillDate.year &&
                            (service.start.month < tillDate.month ||
                                    (service.start.month == tillDate.month && service.start.dayOfMonth <= tillDate.dayOfMonth))
                    }
                    ?.apply {
                        actual += service.minutes / 60.0
                        size++
                }

                eval.actualYear
                    .firstOrNull { it.hourType.id == service.hourType.id &&
                            service.start.year == tillDate.year
                    }
                    ?.apply {
                        actual += service.minutes / 60.0
                        size++
                    }

                eval.actualMonth
                    .firstOrNull { it.hourType.id == service.hourType.id &&
                            service.start.year == tillDate.year &&
                            service.start.month == tillDate.month
                    }
                    ?.apply {
                        actual += service.minutes / 60.0
                        size++
                    }
            }
        }

        return eval
    }
}