package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanCreateDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanUpdateDto
import de.vinz.openfls.domains.assistancePlans.dtos.ActualTargetValueDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanEvalDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanUpdateGoalDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanUpdateHourDto
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.goals.entities.GoalHour
import de.vinz.openfls.domains.goals.repositories.GoalHourRepository
import de.vinz.openfls.domains.goals.repositories.GoalRepository
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanProjection
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanHourRepository
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.hourTypes.HourTypeDto
import de.vinz.openfls.domains.hourTypes.HourTypeService
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.domains.services.services.ServiceService
import de.vinz.openfls.domains.sponsors.SponsorService
import org.modelmapper.ModelMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class AssistancePlanService(
        private val assistancePlanRepository: AssistancePlanRepository,
        private val assistancePlanHourRepository: AssistancePlanHourRepository,
        private val goalRepository: GoalRepository,
        private val goalHourRepository: GoalHourRepository,
        private val serviceService: ServiceService,
        private val clientService: ClientService,
        private val institutionService: InstitutionService,
        private val sponsorService: SponsorService,
        private val hourTypeService: HourTypeService,
        private val modelMapper: ModelMapper
) {

    @Transactional
    fun create(valueDto: AssistancePlanCreateDto): AssistancePlanDto {
        validateHoursPlacement(valueDto)

        val entity = AssistancePlan().apply {
            start = valueDto.start
            end = valueDto.end
        }

        entity.client = clientService.getById(valueDto.clientId)
                ?: throw IllegalArgumentException("client [id = ${valueDto.clientId}] not found")
        entity.institution = institutionService.getEntityById(valueDto.institutionId)
                ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
        entity.sponsor = sponsorService.getById(valueDto.sponsorId)
                ?: throw IllegalArgumentException("sponsor [id = ${valueDto.sponsorId}] not found")
        entity.hours = valueDto.hours
                .map { hourDto ->
                    AssistancePlanHour().apply {
                        weeklyMinutes = hourDto.weeklyMinutes
                        hourType = hourTypeService.getById(hourDto.hourTypeId)
                            ?: throw IllegalArgumentException("hour type with id ${hourDto.hourTypeId} not found")
                        assistancePlan = entity
                    }
                }
                .toMutableSet()

        entity.goals = valueDto.goals.map { goalDto ->
            val goalEntity = Goal().apply {
                title = goalDto.title
                description = goalDto.description
                assistancePlan = entity
                institution = goalDto.institutionId?.let { institutionId ->
                    institutionService.getEntityById(institutionId)
                        ?: throw IllegalArgumentException("institution [id = $institutionId] not found")
                }
            }
            goalEntity.hours = goalDto.hours.map { hourDto ->
                GoalHour().apply {
                    weeklyMinutes = hourDto.weeklyMinutes
                    hourType = hourTypeService.getById(hourDto.hourTypeId)
                        ?: throw IllegalArgumentException("hour type with id ${hourDto.hourTypeId} not found")
                    goal = goalEntity
                }
            }.toMutableSet()
            goalEntity
        }.toMutableSet()

        val savedEntity = assistancePlanRepository.save(entity)

        return modelMapper.map(savedEntity, AssistancePlanDto::class.java)
    }

    private fun validateHoursPlacement(valueDto: AssistancePlanCreateDto) {
        val hasPlanHours = valueDto.hours.isNotEmpty()
        val hasGoalHours = valueDto.goals.any { it.hours.isNotEmpty() }

        if (hasPlanHours && hasGoalHours) {
            throw IllegalArgumentException(
                "Stunden dürfen entweder direkt im Hilfeplan oder in den Zielen hinterlegt sein, nicht in beiden Bereichen gleichzeitig."
            )
        }
    }

    @Transactional
    fun update(id: Long, valueDto: AssistancePlanUpdateDto): AssistancePlanDto {
        if (id != valueDto.id)
            throw IllegalArgumentException("path id and dto id are not the same")
        if (!existsById(id))
            throw IllegalArgumentException("assistance plan not found")
        validateHoursPlacement(valueDto)

        val entity = assistancePlanRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("assistance plan not found")

        entity.start = valueDto.start
        entity.end = valueDto.end

        entity.client = clientService.getById(valueDto.clientId)
                ?: throw IllegalArgumentException("client [id = ${valueDto.clientId}] not found")
        entity.institution = institutionService.getEntityById(valueDto.institutionId)
                ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
        entity.sponsor = sponsorService.getById(valueDto.sponsorId)
                ?: throw IllegalArgumentException("sponsor [id = ${valueDto.sponsorId}] not found")

        val planHours = valueDto.hours
            .map { hourDto ->
                AssistancePlanHour().apply {
                    this.id = hourDto.id
                    weeklyMinutes = hourDto.weeklyMinutes
                    hourType = hourTypeService.getById(hourDto.hourTypeId)
                        ?: throw IllegalArgumentException("hour type with id ${hourDto.hourTypeId} not found")
                    assistancePlan = entity
                }
            }

        assistancePlanHourRepository
            .findByAssistancePlanId(id)
            .filter { existingHour -> !planHours.any { hour -> hour.id > 0 && hour.id == existingHour.id } }
            .forEach { assistancePlanHourRepository.deleteById(it.id) }

        entity.hours = planHours
            .map { hour ->
                assistancePlanHourRepository.save(hour.apply {
                    assistancePlan = entity
                })
            }
            .toMutableSet()

        val goals = valueDto.goals.map { goalDto ->
            val goalEntity = Goal().apply {
                this.id = goalDto.id
                title = goalDto.title
                description = goalDto.description
                assistancePlan = entity
                institution = goalDto.institutionId?.let { institutionId ->
                    institutionService.getEntityById(institutionId)
                        ?: throw IllegalArgumentException("institution [id = $institutionId] not found")
                }
            }

            val savedGoal = goalRepository.save(goalEntity)
            val goalHours = goalDto.hours.map { hourDto ->
                GoalHour().apply {
                    this.id = hourDto.id
                    weeklyMinutes = hourDto.weeklyMinutes
                    hourType = hourTypeService.getById(hourDto.hourTypeId)
                        ?: throw IllegalArgumentException("hour type with id ${hourDto.hourTypeId} not found")
                    goal = savedGoal
                }
            }

            goalHourRepository
                .findByGoalId(savedGoal.id)
                .filter { existingHour -> !goalHours.any { hour -> hour.id > 0 && hour.id == existingHour.id } }
                .forEach { goalHourRepository.deleteById(it.id) }

            savedGoal.hours = goalHours.map { hour ->
                goalHourRepository.save(hour.apply {
                    goal = savedGoal
                })
            }.toMutableSet()

            savedGoal
        }

        goalRepository
            .findByAssistancePlanId(id)
            .filter { existingGoal -> !goals.any { goal -> goal.id > 0 && goal.id == existingGoal.id } }
            .forEach { goalRepository.deleteById(it.id) }

        entity.goals = goals.toMutableSet()

        val savedEntity = assistancePlanRepository.save(entity)

        return modelMapper.map(savedEntity, AssistancePlanDto::class.java)
    }

    private fun validateHoursPlacement(valueDto: AssistancePlanUpdateDto) {
        val hasPlanHours = valueDto.hours.isNotEmpty()
        val hasGoalHours = valueDto.goals.any { it.hours.isNotEmpty() }

        if (hasPlanHours && hasGoalHours) {
            throw IllegalArgumentException(
                "Stunden dürfen entweder direkt im Hilfeplan oder in den Zielen hinterlegt sein, nicht in beiden Bereichen gleichzeitig."
            )
        }
    }

    @Transactional
    fun delete(id: Long) {
        return assistancePlanRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAllAssistancePlanDtos(): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findAll().toList()
        return entities.map { modelMapper.map(it, AssistancePlanDto::class.java) }
    }

    @Transactional(readOnly = true)
    fun getAssistancePlanDtoById(id: Long): AssistancePlanDto? {
        val entity = assistancePlanRepository.findByIdOrNull(id)
        return modelMapper.map(entity, AssistancePlanDto::class.java)
    }

    @Transactional(readOnly = true)
    fun getProjectionById(id: Long): AssistancePlanProjection? {
        return assistancePlanRepository.findProjectionById(id)
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): AssistancePlan? {
        return assistancePlanRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return assistancePlanRepository.existsById(id)
    }

    @Transactional(readOnly = true)
    fun getAssistancePlanDtosByClientId(id: Long): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findByClientId(id)
        return entities.map { modelMapper.map(it, AssistancePlanDto::class.java) }
    }

    @Transactional(readOnly = true)
    fun getIllegalByClientId(id: Long): List<AssistancePlanProjection> {
        val assistancePlans = assistancePlanRepository.findProjectionsByClientId(id)
        return assistancePlans.filter { isIllegalAssistancePlan(it) }
    }

    @Transactional(readOnly = true)
    fun getAssistancePlanDtosBySponsorId(id: Long): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findBySponsorId(id)
        return entities.map { modelMapper.map(it, AssistancePlanDto::class.java) }
    }

    @Transactional(readOnly = true)
    fun getIllegalBySponsorId(id: Long): List<AssistancePlanProjection> {
        val assistancePlans = assistancePlanRepository.findProjectionsBySponsorId(id)
        return assistancePlans.filter { isIllegalAssistancePlan(it) }
    }

    @Transactional(readOnly = true)
    fun getAssistancePlanDtosByInstitutionId(id: Long): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findByInstitutionId(id)
        return entities.map { modelMapper.map(it, AssistancePlanDto::class.java) }
    }

    @Transactional(readOnly = true)
    fun getIllegalByInstitutionId(id: Long): List<AssistancePlanProjection> {
        val assistancePlans = assistancePlanRepository.findProjectionsByInstitutionId(id)
        return assistancePlans.filter { isIllegalAssistancePlan(it) }
    }

    @Transactional(readOnly = true)
    fun getProjectionByYearMonth(year: Int,
                                 month: Int): List<AssistancePlanProjection> {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return assistancePlanRepository.findProjectionByStartAndEnd(start, end)
    }

    @Transactional(readOnly = true)
    fun getProjectionByYearMonthSponsorId(year: Int,
                                          month: Int,
                                          sponsorId: Long): List<AssistancePlanProjection> {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return assistancePlanRepository.findProjectionBySponsorIdAndStartAndEnd(sponsorId, start, end)
    }

    @Transactional(readOnly = true)
    fun getProjectionByYearMonthInstitutionId(year: Int,
                                              month: Int,
                                              institutionId: Long): List<AssistancePlanProjection> {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return assistancePlanRepository.findProjectionByInstitutionIdAndStartAndEnd(institutionId, start, end)
    }

    @Transactional(readOnly = true)
    fun getProjectionByYearMonthInstitutionIdSponsorId(year: Int,
                                                       month: Int,
                                                       institutionId: Long,
                                                       sponsorId: Long): List<AssistancePlanProjection> {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return assistancePlanRepository.findProjectionByInstitutionIdAndSponsorIdAndStartAndEnd(institutionId, sponsorId, start, end)
    }

    @Transactional(readOnly = true)
    fun getEvaluationById(id: Long): AssistancePlanEvalDto {
        val assistancePlan = assistancePlanRepository.findById(id).orElseThrow { IllegalArgumentException("id not found ") }
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

            // service is in between the start and end inclusive
            if ((assistancePlan.start.isBefore(startDate) || assistancePlan.start.isEqual(startDate)) &&
                    (assistancePlan.end.isAfter(startDate) || assistancePlan.end.isEqual(startDate))) {
                // total values
                eval.total
                        .firstOrNull { it.hourType.id == service.hourType?.id }
                        ?.apply {
                            actual += service.minutes / 60.0
                            size++
                        }

                // till today
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
                    // actual year
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
                        // actual year
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
                        // actual month
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

    private fun isIllegalAssistancePlan(assistancePlan: AssistancePlanProjection): Boolean {
        val containsHoursAndGoalHours = assistancePlan.hours.isNotEmpty() && assistancePlan.goals.isNotEmpty() && assistancePlan.goals.any { goal -> goal.hours.isNotEmpty() }
        val containsNoHoursAndNoGoalHours = assistancePlan.hours.isEmpty() && (
                (assistancePlan.goals.isNotEmpty() && assistancePlan.goals.all { goal -> goal.hours.isEmpty() }) ||
                        assistancePlan.goals.isEmpty())
        return containsHoursAndGoalHours || containsNoHoursAndNoGoalHours
    }
}
