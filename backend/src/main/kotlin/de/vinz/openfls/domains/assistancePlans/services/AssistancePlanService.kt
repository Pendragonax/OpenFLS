package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourDto
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanProjection
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanHourRepository
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.hourTypes.HourTypeService
import de.vinz.openfls.domains.assistancePlans.dtos.ActualTargetValueDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanEvalDto
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeDto
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.domains.services.ServiceService
import de.vinz.openfls.domains.sponsors.SponsorService
import de.vinz.openfls.services.*
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class AssistancePlanService(
        private val assistancePlanRepository: AssistancePlanRepository,
        private val assistancePlanHourRepository: AssistancePlanHourRepository,
        private val serviceService: ServiceService,
        private val clientService: ClientService,
        private val institutionService: InstitutionService,
        private val sponsorService: SponsorService,
        private val hourTypeService: HourTypeService,
        private val modelMapper: ModelMapper
) : GenericService<AssistancePlan> {

    private val logger: Logger = LoggerFactory.getLogger(AssistancePlanService::class.java)

    @Transactional
    fun create(valueDto: AssistancePlanDto): AssistancePlanDto {
        val entity = modelMapper.map(valueDto, AssistancePlan::class.java)

        entity.client = clientService.getById(valueDto.clientId)
                ?: throw IllegalArgumentException("client [id = ${valueDto.clientId}] not found")
        entity.institution = institutionService.getById(valueDto.institutionId)
                ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
        entity.sponsor = sponsorService.getById(valueDto.sponsorId)
                ?: throw IllegalArgumentException("sponsor [id = ${valueDto.sponsorId}] not found")
        entity.hours = valueDto.hours
                .map { modelMapper.map(it, AssistancePlanHour::class.java) }
                .map { it.apply {
                    hourType = hourTypeService.getById(it.hourType!!.id)
                            ?: throw IllegalArgumentException("hour type with id ${hourType?.id} not found")
                } }
                .toMutableSet()

        val savedEntity = this.create(entity)

        valueDto.apply {
            id = savedEntity.id
            hours = savedEntity.hours
                    .map { modelMapper.map(it, AssistancePlanHourDto::class.java) }
                    .toMutableSet()
        }

        return valueDto
    }

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
                .map {
                    assistancePlanHourRepository
                            .save(it.apply {
                                id = 0
                                assistancePlan = entity
                            })
                }
                .toMutableSet()

        return entity
    }

    @Transactional
    fun update(id: Long, valueDto: AssistancePlanDto): AssistancePlanDto {
        if (id != valueDto.id)
            throw IllegalArgumentException("path id and dto id are not the same")
        if (!existsById(id))
            throw IllegalArgumentException("assistance plan not found")

        val entity = modelMapper.map(valueDto, AssistancePlan::class.java)

        entity.client = clientService.getById(valueDto.clientId)
                ?: throw IllegalArgumentException("client [id = ${valueDto.clientId}] not found")
        entity.institution = institutionService.getById(valueDto.institutionId)
                ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
        entity.sponsor = sponsorService.getById(valueDto.sponsorId)
                ?: throw IllegalArgumentException("sponsor [id = ${valueDto.sponsorId}] not found")
        entity.hours = valueDto.hours
                .map { modelMapper.map(it, AssistancePlanHour::class.java) }
                .map { it.apply {
                    hourType = hourTypeService.getById(it.hourType!!.id)
                            ?: throw IllegalArgumentException("hour type with id ${hourType!!.id} not found")
                } }
                .toMutableSet()

        entity.hours.forEach { logger.info(it.id.toString())}

        val savedEntity = update(entity)

        savedEntity.hours.forEach { logger.info(it.id.toString())}

        valueDto.apply {
            this.id = savedEntity.id
            hours = savedEntity.hours
                    .map { modelMapper.map(it, AssistancePlanHourDto::class.java) }
                    .toMutableSet()
        }

        return valueDto
    }

    @Transactional
    override fun update(value: AssistancePlan): AssistancePlan {
        if (value.id <= 0)
            throw IllegalArgumentException("id is set")
        if (!assistancePlanRepository.existsById(value.id))
            throw IllegalArgumentException("id not found")

        // backup hours
        value.hours.forEach { logger.info(it.id.toString())}
        value.goals = mutableSetOf()

        return assistancePlanRepository.save(value)
    }

    @Transactional
    override fun delete(id: Long) {
        return assistancePlanRepository.deleteById(id)
    }

    fun getAllAssistancePlanDtos(): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findAll().toList()
        return entities.map { modelMapper.map(it, AssistancePlanDto::class.java) }
    }

    override fun getAll(): List<AssistancePlan> {
        return assistancePlanRepository.findAll().toList()
    }

    fun getAssistancePlanDtoById(id: Long): AssistancePlanDto? {
        val entity = assistancePlanRepository.findByIdOrNull(id)
        return modelMapper.map(entity, AssistancePlanDto::class.java)
    }

    fun getProjectionById(id: Long): AssistancePlanProjection? {
        return assistancePlanRepository.findProjectionById(id)
    }

    override fun getById(id: Long): AssistancePlan? {
        return assistancePlanRepository.findByIdOrNull(id)
    }

    override fun existsById(id: Long): Boolean {
        return assistancePlanRepository.existsById(id)
    }

    fun getAssistancePlanDtosByClientId(id: Long): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findByClientId(id)
        return entities.map { modelMapper.map(it, AssistancePlanDto::class.java) }
    }

    fun getByClientId(id: Long): List<AssistancePlan> {
        return assistancePlanRepository.findByClientId(id)
    }

    fun getIllegalByClientId(id: Long): List<AssistancePlanProjection> {
        val assistancePlans = assistancePlanRepository.findProjectionsByClientId(id)
        return assistancePlans.filter { isIllegalAssistancePlan(it) }
    }

    fun getAssistancePlanDtosBySponsorId(id: Long): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findBySponsorId(id)
        return entities.map { modelMapper.map(it, AssistancePlanDto::class.java) }
    }

    fun getBySponsorId(id: Long): List<AssistancePlan> {
        return assistancePlanRepository.findBySponsorId(id)
    }

    fun getIllegalBySponsorId(id: Long): List<AssistancePlanProjection> {
        val assistancePlans = assistancePlanRepository.findProjectionsBySponsorId(id)
        return assistancePlans.filter { isIllegalAssistancePlan(it) }
    }

    fun getAssistancePlanDtosByInstitutionId(id: Long): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findByInstitutionId(id)
        return entities.map { modelMapper.map(it, AssistancePlanDto::class.java) }
    }

    fun getByInstitutionId(id: Long): List<AssistancePlan> {
        return assistancePlanRepository.findByInstitutionId(id)
    }

    fun getIllegalByInstitutionId(id: Long): List<AssistancePlanProjection> {
        val assistancePlans = assistancePlanRepository.findProjectionsByInstitutionId(id)
        return assistancePlans.filter { isIllegalAssistancePlan(it) }
    }

    fun getProjectionByYearMonth(year: Int,
                                 month: Int): List<AssistancePlanProjection> {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return assistancePlanRepository.findProjectionByStartAndEnd(start, end)
    }

    fun getProjectionByYearMonthSponsorId(year: Int,
                                          month: Int,
                                          sponsorId: Long): List<AssistancePlanProjection> {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return assistancePlanRepository.findProjectionBySponsorIdAndStartAndEnd(sponsorId, start, end)
    }

    fun getProjectionByYearMonthInstitutionId(year: Int,
                                              month: Int,
                                              institutionId: Long): List<AssistancePlanProjection> {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return assistancePlanRepository.findProjectionByInstitutionIdAndStartAndEnd(institutionId, start, end)
    }

    fun getProjectionByYearMonthInstitutionIdSponsorId(year: Int,
                                                       month: Int,
                                                       institutionId: Long,
                                                       sponsorId: Long): List<AssistancePlanProjection> {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return assistancePlanRepository.findProjectionByInstitutionIdAndSponsorIdAndStartAndEnd(institutionId, sponsorId, start, end)
    }

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