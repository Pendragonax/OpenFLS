package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanCreateDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanUpdateDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.goals.entities.GoalHour
import de.vinz.openfls.domains.goals.repositories.GoalHourRepository
import de.vinz.openfls.domains.goals.repositories.GoalRepository
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanProjection
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanHourRepository
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.hourCorridors.HourCorridorRepository
import de.vinz.openfls.domains.hourTypes.HourTypeService
import de.vinz.openfls.domains.institutions.InstitutionService
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
        private val hourCorridorRepository: HourCorridorRepository,
        private val goalRepository: GoalRepository,
        private val goalHourRepository: GoalHourRepository,
        private val clientService: ClientService,
        private val institutionService: InstitutionService,
        private val sponsorService: SponsorService,
        private val hourTypeService: HourTypeService,
        private val modelMapper: ModelMapper
) {

    @Transactional
    fun create(valueDto: AssistancePlanCreateDto): AssistancePlanDto {
        validateModeAndHours(valueDto)

        val entity = AssistancePlan().apply {
            start = valueDto.start
            end = valueDto.end
            hourMode = valueDto.hourMode
            hourCorridor = resolveHourCorridor(valueDto)
        }

        entity.client = clientService.getById(valueDto.clientId)
                ?: throw IllegalArgumentException("client [id = ${valueDto.clientId}] not found")
        if (entity.client?.archived == true) {
            throw IllegalStateException("client is archived")
        }
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

        return mapToDto(savedEntity)
    }

    private fun validateModeAndHours(valueDto: AssistancePlanCreateDto) {
        if (valueDto.hourMode == AssistancePlanHourMode.CORRIDOR) {
            validateCorridorModeInput(
                hourCorridorId = valueDto.hourCorridorId,
                hoursPresent = valueDto.hours.isNotEmpty(),
                goalHoursPresent = valueDto.goals.any { it.hours.isNotEmpty() }
            )
            return
        }

        val hasPlanHours = valueDto.hours.isNotEmpty()
        val hasGoalHours = valueDto.goals.any { it.hours.isNotEmpty() }

        if (hasPlanHours && hasGoalHours) {
            throw IllegalArgumentException(
                "Stunden dürfen entweder direkt im Hilfeplan oder in den Zielen hinterlegt sein, nicht in beiden Bereichen gleichzeitig."
            )
        }

        if (valueDto.hourCorridorId > 0) {
            throw IllegalArgumentException("exact assistance plans must not reference an hour corridor")
        }
    }

    @Transactional
    fun update(id: Long, valueDto: AssistancePlanUpdateDto): AssistancePlanDto {
        if (id != valueDto.id)
            throw IllegalArgumentException("path id and dto id are not the same")
        if (!existsById(id))
            throw IllegalArgumentException("assistance plan not found")

        val entity = assistancePlanRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("assistance plan not found")
        validateModeAndHours(entity, valueDto)

        entity.start = valueDto.start
        entity.end = valueDto.end
        entity.hourMode = valueDto.hourMode
        entity.hourCorridor = resolveHourCorridor(valueDto)

        entity.client = clientService.getById(valueDto.clientId)
                ?: throw IllegalArgumentException("client [id = ${valueDto.clientId}] not found")
        if (entity.client?.archived == true) {
            throw IllegalStateException("client is archived")
        }
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

        return mapToDto(savedEntity)
    }

    private fun validateModeAndHours(entity: AssistancePlan, valueDto: AssistancePlanUpdateDto) {
        if (entity.hourMode != valueDto.hourMode) {
            throw IllegalArgumentException("assistance plan hour mode cannot be changed")
        }

        if (entity.hourMode == AssistancePlanHourMode.CORRIDOR) {
            validateCorridorModeInput(
                hourCorridorId = valueDto.hourCorridorId,
                hoursPresent = valueDto.hours.isNotEmpty(),
                goalHoursPresent = valueDto.goals.any { it.hours.isNotEmpty() }
            )
            return
        }

        val hasPlanHours = valueDto.hours.isNotEmpty()
        val hasGoalHours = valueDto.goals.any { it.hours.isNotEmpty() }

        if (!hasPlanHours || !hasGoalHours) {
            if (valueDto.hourCorridorId > 0) {
                throw IllegalArgumentException("exact assistance plans must not reference an hour corridor")
            }
            return
        }

        val existingHasPlanHours = entity.hours.isNotEmpty()
        val existingHasGoalHours = entity.goals.any { it.hours.isNotEmpty() }

        if (!existingHasPlanHours || !existingHasGoalHours) {
            throw IllegalArgumentException(
                "Stunden dürfen entweder direkt im Hilfeplan oder in den Zielen hinterlegt sein, nicht in beiden Bereichen gleichzeitig."
            )
        }

        val existingPlanHourIds = entity.hours.map { it.id }.toSet()
        val existingGoalHourIds = entity.goals
            .flatMap { goal -> goal.hours.map { hour -> hour.id } }
            .toSet()

        val hasNewPlanHours = valueDto.hours.any { hour -> hour.id <= 0 || !existingPlanHourIds.contains(hour.id) }
        val hasNewGoalHours = valueDto.goals
            .flatMap { goal -> goal.hours }
            .any { hour -> hour.id <= 0 || !existingGoalHourIds.contains(hour.id) }

        if (hasNewPlanHours || hasNewGoalHours) {
            throw IllegalArgumentException(
                "Bei Hilfeplänen mit Stunden in beiden Bereichen dürfen keine neuen Stunden hinzugefügt werden. Bitte erst bestehende Stunden löschen, bis nur noch ein Bereich Stunden enthält."
            )
        }

        if (valueDto.hourCorridorId > 0) {
            throw IllegalArgumentException("exact assistance plans must not reference an hour corridor")
        }
    }

    @Transactional
    fun delete(id: Long) {
        return assistancePlanRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAllAssistancePlanDtos(
        includeArchived: Boolean = false,
        leadingInstitutionIds: List<Long> = emptyList()
    ): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findAll().toList()
        return entities
            .filter { isVisible(it.client?.archived == true, it.client?.institution?.id, includeArchived, leadingInstitutionIds) }
            .map(::mapToDto)
    }

    @Transactional(readOnly = true)
    fun getAssistancePlanDtoById(
        id: Long,
        includeArchived: Boolean = false,
        leadingInstitutionIds: List<Long> = emptyList()
    ): AssistancePlanDto? {
        val entity = assistancePlanRepository.findByIdOrNull(id)
        return entity?.takeIf { isVisible(it.client?.archived == true, it.client?.institution?.id, includeArchived, leadingInstitutionIds) }
            ?.let(::mapToDto)
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
    fun getAssistancePlanDtosByClientId(
        id: Long,
        includeArchived: Boolean = false,
        leadingInstitutionIds: List<Long> = emptyList()
    ): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findByClientId(id)
        return entities
            .filter { isVisible(it.client?.archived == true, it.client?.institution?.id, includeArchived, leadingInstitutionIds) }
            .map(::mapToDto)
    }

    @Transactional(readOnly = true)
    fun getIllegalByClientId(id: Long): List<AssistancePlanProjection> {
        val assistancePlans = assistancePlanRepository.findProjectionsByClientId(id)
        return assistancePlans.filter { isIllegalAssistancePlan(it) }
    }

    @Transactional(readOnly = true)
    fun getAssistancePlanDtosBySponsorId(
        id: Long,
        includeArchived: Boolean = false,
        leadingInstitutionIds: List<Long> = emptyList()
    ): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findBySponsorId(id)
        return entities
            .filter { isVisible(it.client?.archived == true, it.client?.institution?.id, includeArchived, leadingInstitutionIds) }
            .map(::mapToDto)
    }

    @Transactional(readOnly = true)
    fun getIllegalBySponsorId(id: Long): List<AssistancePlanProjection> {
        val assistancePlans = assistancePlanRepository.findProjectionsBySponsorId(id)
        return assistancePlans.filter { isIllegalAssistancePlan(it) }
    }

    @Transactional(readOnly = true)
    fun getAssistancePlanDtosByInstitutionId(
        id: Long,
        includeArchived: Boolean = false,
        leadingInstitutionIds: List<Long> = emptyList()
    ): List<AssistancePlanDto> {
        val entities = assistancePlanRepository.findByInstitutionId(id)
        return entities
            .filter { isVisible(it.client?.archived == true, it.client?.institution?.id, includeArchived, leadingInstitutionIds) }
            .map(::mapToDto)
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

    private fun isIllegalAssistancePlan(assistancePlan: AssistancePlanProjection): Boolean {
        return when (assistancePlan.hourMode) {
            AssistancePlanHourMode.CORRIDOR -> {
                assistancePlan.hours.isNotEmpty() || assistancePlan.goals.any { goal -> goal.hours.isNotEmpty() }
            }
            AssistancePlanHourMode.EXACT -> {
                val containsHoursAndGoalHours = assistancePlan.hours.isNotEmpty() &&
                    assistancePlan.goals.isNotEmpty() &&
                    assistancePlan.goals.any { goal -> goal.hours.isNotEmpty() }
                val containsNoHoursAndNoGoalHours = assistancePlan.hours.isEmpty() && (
                    (assistancePlan.goals.isNotEmpty() && assistancePlan.goals.all { goal -> goal.hours.isEmpty() }) ||
                        assistancePlan.goals.isEmpty()
                    )
                containsHoursAndGoalHours || containsNoHoursAndNoGoalHours
            }
        }
    }

    private fun mapToDto(entity: AssistancePlan): AssistancePlanDto {
        val dto = modelMapper.map(entity, AssistancePlanDto::class.java)
        dto.institutionName = entity.institution?.name ?: ""
        dto.clientArchived = entity.client?.archived ?: false
        dto.hourMode = entity.hourMode
        dto.hourCorridorId = entity.hourCorridor?.id ?: 0
        return dto
    }

    private fun resolveHourCorridor(valueDto: AssistancePlanCreateDto): de.vinz.openfls.domains.hourCorridors.HourCorridor? {
        if (valueDto.hourMode != AssistancePlanHourMode.CORRIDOR) {
            return null
        }

        return hourCorridorRepository.findByIdOrNull(valueDto.hourCorridorId)
            ?: throw IllegalArgumentException("hour corridor with id ${valueDto.hourCorridorId} not found")
    }

    private fun resolveHourCorridor(valueDto: AssistancePlanUpdateDto): de.vinz.openfls.domains.hourCorridors.HourCorridor? {
        if (valueDto.hourMode != AssistancePlanHourMode.CORRIDOR) {
            return null
        }

        return hourCorridorRepository.findByIdOrNull(valueDto.hourCorridorId)
            ?: throw IllegalArgumentException("hour corridor with id ${valueDto.hourCorridorId} not found")
    }

    private fun validateCorridorModeInput(
        hourCorridorId: Long,
        hoursPresent: Boolean,
        goalHoursPresent: Boolean
    ) {
        if (hourCorridorId <= 0) {
            throw IllegalArgumentException("corridor assistance plans require an hour corridor")
        }
        if (hoursPresent) {
            throw IllegalArgumentException("corridor assistance plans must not contain plan hours")
        }
        if (goalHoursPresent) {
            throw IllegalArgumentException("corridor assistance plans must not contain goal hours")
        }
    }

    private fun isVisible(
        archived: Boolean,
        institutionId: Long?,
        includeArchived: Boolean,
        leadingInstitutionIds: List<Long>
    ): Boolean {
        return !archived || includeArchived || leadingInstitutionIds.contains(institutionId ?: 0)
    }
}
