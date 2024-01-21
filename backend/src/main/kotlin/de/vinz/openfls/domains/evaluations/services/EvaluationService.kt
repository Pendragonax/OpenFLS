package de.vinz.openfls.domains.evaluations.services

import de.vinz.openfls.domains.evaluations.dtos.EvaluationMonthResponseDto
import de.vinz.openfls.domains.evaluations.dtos.EvaluationRequestDto
import de.vinz.openfls.domains.evaluations.dtos.EvaluationResponseDto
import de.vinz.openfls.domains.evaluations.dtos.GoalEvaluationsYearDto
import de.vinz.openfls.domains.evaluations.entities.Evaluation
import de.vinz.openfls.domains.evaluations.repositories.EvaluationRepository
import de.vinz.openfls.dtos.GenericYearlyResponseDto
import de.vinz.openfls.entities.Employee
import de.vinz.openfls.entities.Goal
import de.vinz.openfls.repositories.AssistancePlanRepository
import de.vinz.openfls.services.DateService
import org.modelmapper.ModelMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@Service
class EvaluationService(
        private val evaluationRepository: EvaluationRepository,
        private val assistancePlanRepository: AssistancePlanRepository,
        private val modelMapper: ModelMapper
) {

    fun create(value: EvaluationRequestDto, createdBy: Employee): EvaluationResponseDto {
        val entity = modelMapper.map(value, Evaluation::class.java)

        // reset id
        entity.id = 0

        // user info
        entity.createdBy = createdBy
        entity.createdAt = LocalDateTime.now()

        return saveEvaluation(entity, createdBy)
    }

    fun update(value: EvaluationRequestDto, updatedBy: Employee): EvaluationResponseDto {
        val existingEntity =
                evaluationRepository
                        .findById(value.id)
                        .orElseThrow { IllegalArgumentException("Unknown evaluation id") }

        existingEntity.apply {
            content = value.content
            date = value.date
            approved = value.approved
        }

        // user info
        return saveEvaluation(existingEntity, updatedBy)
    }

    fun delete(id: Long) {
        return evaluationRepository.deleteById(id)
    }

    fun getAll(): List<EvaluationResponseDto> {
        val entities = evaluationRepository.findAll().toList()

        return entities.map { convertEntityToDto(it) }
    }

    fun getById(id: Long): EvaluationResponseDto? {
        return evaluationRepository.findByIdOrNull(id)?.let { convertEntityToDto(it) }
    }

    fun getByGoalId(goalId: Long): List<EvaluationResponseDto> {
        val evaluations = evaluationRepository.findAllByGoalId(goalId)

        return evaluations.map { convertEntityToDto(it) }
    }

    fun getByAssistancePlanId(assistancePlanId: Long): List<EvaluationResponseDto>? {
        val assistancePlan = assistancePlanRepository.findById(assistancePlanId)

        if (assistancePlan.isPresent) {
            val goalIds = assistancePlan.get().goals.map { it.id }
            val entities = evaluationRepository.findAllByGoalIdIn(goalIds)
            return entities.map { convertEntityToDto(it) }.sortedBy { it.goalId }
        }

        return null
    }

    fun getByAssistancePlanIdAndYear(assistancePlanId: Long, year: Int): GenericYearlyResponseDto<GoalEvaluationsYearDto>? {
        val assistancePlan = assistancePlanRepository
                .findById(assistancePlanId)
                .orElseThrow { IllegalArgumentException("no assistance plan found") }
        val goalEvaluations = assistancePlan.goals
                .map { getGoalEvaluationsYearly(it, year, assistancePlan.start, assistancePlan.end) }
                .sortedBy { it.goalId }
        return GenericYearlyResponseDto<GoalEvaluationsYearDto>().also {
            it.year = year
            it.values = goalEvaluations
        }
    }

    fun existsById(id: Long): Boolean {
        return evaluationRepository.existsById(id)
    }

    private fun saveEvaluation(entity: Evaluation, employee: Employee): EvaluationResponseDto {
        entity.updatedBy = employee
        entity.updatedAt = LocalDateTime.now()

        val savedEntity = evaluationRepository.save(entity)
        return convertEntityToDto(savedEntity)
    }

    private fun convertEntityToDto(entity: Evaluation): EvaluationResponseDto {
        val responseDto = modelMapper.map(entity, EvaluationResponseDto::class.java)
        responseDto.createdBy = entity.createdBy.lastname + " " + entity.createdBy.firstname
        responseDto.updatedBy = entity.updatedBy.lastname + " " + entity.updatedBy.firstname

        return responseDto
    }

    private fun getGoalEvaluationsYearly(goal: Goal, year: Int, start: LocalDate, end: LocalDate): GoalEvaluationsYearDto {
        val evaluations = getByGoalId(goal.id)

        val monthlyEvaluations = List(12) { EvaluationMonthResponseDto() }
        for (i in 1..12) {
            val yearMonth = YearMonth.of(year, i)
            monthlyEvaluations[i - 1].assistancePlanActive = DateService.isYearMonthInBetweenInclusive(yearMonth, start, end)
            monthlyEvaluations[i - 1].month = i
            monthlyEvaluations[i - 1].evaluation = evaluations.firstOrNull { YearMonth.of(it.date.year, it.date.monthValue) == yearMonth }
        }

        return GoalEvaluationsYearDto().also {
            it.goalId = goal.id
            it.title = goal.title
            it.months = monthlyEvaluations
        }
    }
}