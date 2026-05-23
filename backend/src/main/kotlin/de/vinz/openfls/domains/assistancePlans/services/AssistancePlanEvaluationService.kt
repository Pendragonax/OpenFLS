package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.dtos.ActualTargetValueDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanEvalDto
import de.vinz.openfls.domains.hourTypes.HourTypeDto
import de.vinz.openfls.domains.services.services.ServiceService
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class AssistancePlanEvaluationService(
    private val assistancePlanService: AssistancePlanService,
    private val serviceService: ServiceService,
    private val modelMapper: ModelMapper
) {

    @Transactional(readOnly = true)
    fun getEvaluationById(id: Long): AssistancePlanEvalDto {
        val assistancePlan = assistancePlanService.getById(id) ?: throw IllegalArgumentException("id not found ")
        val services = serviceService.getByAssistancePlan(id)
        val eval = AssistancePlanEvalDto()

        val days = ChronoUnit.DAYS.between(assistancePlan.start, assistancePlan.end) + 1
        val tillDate = if (assistancePlan.end < LocalDate.now()) assistancePlan.end else LocalDate.now()
        val daysTillToday = ChronoUnit.DAYS.between(assistancePlan.start, tillDate) + 1

        val actualMonth = getDaysOfActualMonth(assistancePlan)
        val actualYear = getDaysOfActualYear(assistancePlan)

        eval.total = assistancePlan.hours.map {
            ActualTargetValueDto().apply {
                target = days * (it.weeklyMinutes / 7.0) / 60.0
                hourType = modelMapper.map(it.hourType, HourTypeDto::class.java)
            }
        }

        eval.tillToday = assistancePlan.hours.map {
            ActualTargetValueDto().apply {
                target = daysTillToday * (it.weeklyMinutes / 7.0) / 60.0
                hourType = modelMapper.map(it.hourType, HourTypeDto::class.java)
            }
        }

        eval.actualYear = assistancePlan.hours.map {
            ActualTargetValueDto().apply {
                target = actualYear.first * (it.weeklyMinutes / 7.0) / 60.0
                hourType = modelMapper.map(it.hourType, HourTypeDto::class.java)
            }
        }

        eval.actualMonth = assistancePlan.hours.map {
            ActualTargetValueDto().apply {
                target = actualMonth.first * (it.weeklyMinutes / 7.0) / 60.0
                hourType = modelMapper.map(it.hourType, HourTypeDto::class.java)
            }
        }

        for (service in services) {
            val startDate = service.start.toLocalDate()

            if ((assistancePlan.start.isBefore(startDate) || assistancePlan.start.isEqual(startDate)) &&
                (assistancePlan.end.isAfter(startDate) || assistancePlan.end.isEqual(startDate))) {
                eval.total
                    .firstOrNull { it.hourType.id == service.hourType?.id }
                    ?.apply {
                        actual += service.minutes / 60.0
                        size++
                    }

                eval.tillToday
                    .firstOrNull {
                        it.hourType.id == service.hourType?.id &&
                            service.start.year <= tillDate.year &&
                            (service.start.month < tillDate.month ||
                                (service.start.month == tillDate.month && service.start.dayOfMonth <= tillDate.dayOfMonth))
                    }
                    ?.apply {
                        actual += service.minutes / 60.0
                        size++
                    }

                if (actualYear.second != null && actualYear.third != null) {
                    eval.actualYear
                        .firstOrNull {
                            it.hourType.id == service.hourType?.id &&
                                (startDate.isAfter(actualYear.second) || startDate.isEqual(actualYear.second)) &&
                                (startDate.isBefore(actualYear.third) || startDate.isEqual(actualYear.third))
                        }
                        ?.apply {
                            actual += service.minutes / 60.0
                            size++
                        }

                    if (actualYear.second != null && actualYear.third != null) {
                        eval.actualYear
                            .firstOrNull {
                                it.hourType.id == service.hourType?.id &&
                                    (startDate.isAfter(actualYear.second) || startDate.isEqual(actualYear.second)) &&
                                    (startDate.isBefore(actualYear.third) || startDate.isEqual(actualYear.third))
                            }
                            ?.apply {
                                actual += service.minutes / 60.0
                                size++
                            }
                    }

                    if (actualMonth.second != null && actualMonth.third != null) {
                        eval.actualMonth
                            .firstOrNull {
                                it.hourType.id == service.hourType?.id &&
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
        }
        return eval
    }

    private fun getDaysOfActualMonth(assistancePlan: AssistancePlan): Triple<Long, LocalDate?, LocalDate?> {
        val today = LocalDate.now()
        val firstOfMonth = LocalDate.of(today.year, today.monthValue, 1)
        var from: LocalDate = firstOfMonth
        var till: LocalDate = today

        if ((today.isAfter(assistancePlan.start) || today.isEqual(assistancePlan.start)) &&
            (firstOfMonth.isBefore(assistancePlan.end) || firstOfMonth.isEqual(assistancePlan.end))) {
            if (assistancePlan.start.year == today.year && assistancePlan.start.month == today.month) {
                from = assistancePlan.start
            }

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

        if ((today.isAfter(assistancePlan.start) || today.isEqual(assistancePlan.start)) &&
            (firstOfYear.isBefore(assistancePlan.end) || firstOfYear.isEqual(assistancePlan.end))) {
            if (assistancePlan.start.year == today.year) {
                from = assistancePlan.start
            }

            if (assistancePlan.end < today && assistancePlan.end.year == today.year) {
                till = assistancePlan.end
            }

            return Triple(ChronoUnit.DAYS.between(from, till) + 1, from, till)
        } else {
            return Triple(0, null, null)
        }
    }
}
