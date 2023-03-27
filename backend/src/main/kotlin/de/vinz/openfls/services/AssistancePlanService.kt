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
        val daysTillToday = ChronoUnit.DAYS.between(assistancePlan.start, tillDate) + 1

        val actualMonth = getDaysOfActualMonth(assistancePlan)
        val actualYear = getDaysOfActualYear(assistancePlan)


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
                target = actualYear.first * (it.weeklyHours / 7)
                hourType = modelMapper.map(it.hourType, HourTypeDto::class.java)
            }
        }

        eval.actualMonth = assistancePlan.hours.map {
            ActualTargetValueDto().apply {
                target = actualMonth.first * (it.weeklyHours / 7)
                hourType = modelMapper.map(it.hourType, HourTypeDto::class.java)
            }
        }

        for (service in services) {
            val startDate = service.start.toLocalDate()

            // service is in between the start and end inclusive
            if ((assistancePlan.start.isBefore(startDate) || assistancePlan.start.isEqual(startDate)) &&
                (assistancePlan.end.isAfter(startDate) || assistancePlan.end.isEqual(startDate))) {
                // total values
                eval.total
                    .firstOrNull { it.hourType.id == service.hourType.id }
                    ?.apply {
                        actual += service.minutes / 60.0
                        size++
                    }

                // till today
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

                if (actualYear.second != null && actualYear.third != null) {
                    // actual year
                    eval.actualYear
                        .firstOrNull { it.hourType.id == service.hourType.id &&
                                (startDate.isAfter(actualYear.second) || startDate.isEqual(actualYear.second)) &&
                                (startDate.isBefore(actualYear.third) || startDate.isEqual(actualYear.third))
                        }
                        ?.apply {
                            actual += service.minutes / 60.0
                            size++
                        }
                }

                if (actualMonth.second != null && actualMonth.third != null) {
                    // actual month
                    eval.actualMonth
                        .firstOrNull { it.hourType.id == service.hourType.id &&
                                (startDate.isAfter(actualMonth.second) || startDate.isEqual(actualMonth.second)) &&
                                (startDate.isBefore(actualMonth.third) || startDate.isEqual(actualMonth.third))
                        }
                        ?.apply {
                            actual += service.minutes / 60.0
                            size++
                        }
                }

            } else {
                eval.notMatchingServices++
                eval.notMatchingServicesIds.add(service.id)
            }
        }

        return eval
    }

    private fun getDaysOfActualMonth(assistancePlan: AssistancePlan): Triple<Long, LocalDate?, LocalDate?> {
        val today = LocalDate.now()
        val firstOfMonth = LocalDate.of(today.year, today.monthValue, 1)
        var from: LocalDate = firstOfMonth
        var till: LocalDate = today

        // month is in between start and end
        if ((today.isAfter(assistancePlan.start) || today.isEqual(assistancePlan.start)) &&
            (firstOfMonth.isBefore(assistancePlan.end) || firstOfMonth.isEqual(assistancePlan.end))) {
            // start is in the actual month
            if (assistancePlan.start.year == today.year && assistancePlan.start.month == today.month) {
                from = assistancePlan.start
            }

            // end is in the actual month
            if (assistancePlan.end < today && assistancePlan.end.year == today.year && assistancePlan.end.month == today.month) {
                till = assistancePlan.end
            }

            return Triple(ChronoUnit.DAYS.between(from, till) + 1, from, till)
        } else {
            return Triple(0, null, null)
        }
    }

    private fun getDaysOfActualYear(assistancePlan: AssistancePlan): Triple<Long, LocalDate?, LocalDate?> {
        val today = LocalDate.now()
        val firstOfYear = LocalDate.of(today.year, 1, 1)
        var from: LocalDate = firstOfYear
        var till: LocalDate = today

        // month is in between start and end
        if ((today.isAfter(assistancePlan.start) || today.isEqual(assistancePlan.start)) &&
            (firstOfYear.isBefore(assistancePlan.end) || firstOfYear.isEqual(assistancePlan.end))) {
            // start is in the actual month
            if (assistancePlan.start.year == today.year) {
                from = assistancePlan.start
            }

            // end is in the actual month
            if (assistancePlan.end < today && assistancePlan.end.year == today.year) {
                till = assistancePlan.end
            }

            return Triple(ChronoUnit.DAYS.between(from, till) + 1, from, till)
        } else {
            return Triple(0, null, null)
        }
    }
}