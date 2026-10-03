package de.vinz.openfls.domains.evaluations.service

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.evaluations.dto.EvaluationCreateRequest
import de.vinz.openfls.domains.evaluations.dto.EvaluationCreateResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationDeleteResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationMonthResponse
import de.vinz.openfls.domains.evaluations.dto.EvaluationResponse
import de.vinz.openfls.domains.evaluations.dto.EvaluationUpdateRequest
import de.vinz.openfls.domains.evaluations.dto.EvaluationUpdateResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationYearResponse
import de.vinz.openfls.domains.evaluations.dto.EvaluationYearResult
import de.vinz.openfls.domains.evaluations.dto.GoalEvaluationsYearResponse
import de.vinz.openfls.domains.evaluations.entity.Evaluation
import de.vinz.openfls.domains.evaluations.repository.EvaluationRepository
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.goals.service.GoalService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.common.time.DateService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@Service
class EvaluationService(
    private val evaluationRepository: EvaluationRepository,
    private val goalService: GoalService,
    private val assistancePlanService: AssistancePlanService,
    private val employeeService: EmployeeService,
    private val accessService: AccessService
) {

    private enum class WriteAccess { ALLOWED, FORBIDDEN, CLIENT_ARCHIVED }

    @Transactional
    fun create(request: EvaluationCreateRequest): EvaluationCreateResult {
        val goal = goalService.getEntityById(request.goalId)
            ?: return EvaluationCreateResult.GoalNotFound

        when (checkWriteAccess(goal.assistancePlan)) {
            WriteAccess.FORBIDDEN -> return EvaluationCreateResult.Forbidden
            WriteAccess.CLIENT_ARCHIVED -> return EvaluationCreateResult.ClientArchived
            WriteAccess.ALLOWED -> Unit
        }

        val user = getCurrentEmployee()
        val now = LocalDateTime.now()
        val saved = evaluationRepository.save(
            Evaluation(
                date = request.date,
                content = request.content,
                approved = request.approved,
                createdAt = now,
                createdBy = user,
                updatedAt = now,
                updatedBy = user,
                goal = goal
            )
        )

        return EvaluationCreateResult.Success(EvaluationResponse.from(saved))
    }

    @Transactional
    fun update(request: EvaluationUpdateRequest): EvaluationUpdateResult {
        val existing = evaluationRepository.findByIdOrNull(request.id)
            ?: return EvaluationUpdateResult.NotFound

        when (checkWriteAccess(existing.goal?.assistancePlan)) {
            WriteAccess.FORBIDDEN -> return EvaluationUpdateResult.Forbidden
            WriteAccess.CLIENT_ARCHIVED -> return EvaluationUpdateResult.ClientArchived
            WriteAccess.ALLOWED -> Unit
        }

        existing.apply {
            content = request.content
            date = request.date
            approved = request.approved
            updatedBy = getCurrentEmployee()
            updatedAt = LocalDateTime.now()
        }

        return EvaluationUpdateResult.Success(EvaluationResponse.from(evaluationRepository.save(existing)))
    }

    @Transactional
    fun delete(id: Long): EvaluationDeleteResult {
        val existing = evaluationRepository.findByIdOrNull(id)
            ?: return EvaluationDeleteResult.NotFound

        when (checkWriteAccess(existing.goal?.assistancePlan)) {
            WriteAccess.FORBIDDEN -> return EvaluationDeleteResult.Forbidden
            WriteAccess.CLIENT_ARCHIVED -> return EvaluationDeleteResult.ClientArchived
            WriteAccess.ALLOWED -> Unit
        }

        val response = EvaluationResponse.from(existing)
        evaluationRepository.delete(existing)

        return EvaluationDeleteResult.Success(response)
    }

    @Transactional(readOnly = true)
    fun getYearEvaluationsByAssistancePlanIdAndYear(assistancePlanId: Long, year: Int): EvaluationYearResult {
        val assistancePlan = assistancePlanService.getEntityById(assistancePlanId)
            ?: return EvaluationYearResult.AssistancePlanNotFound

        if (!accessService.canReadEntries(assistancePlan.institution?.id ?: 0)) {
            return EvaluationYearResult.Forbidden
        }

        val goalIds = assistancePlan.goals.map { it.id }
        val evaluationsByGoalId = if (goalIds.isEmpty()) {
            emptyMap()
        } else {
            evaluationRepository.findAllByGoalIdIn(goalIds).groupBy { it.goal?.id }
        }

        val goalEvaluations = assistancePlan.goals
            .map { goal ->
                buildGoalEvaluationsYear(
                    goal, year, assistancePlan.start, assistancePlan.end, evaluationsByGoalId[goal.id].orEmpty()
                )
            }
            .sortedBy { it.goalId }

        return EvaluationYearResult.Success(EvaluationYearResponse(year, goalEvaluations))
    }

    private fun checkWriteAccess(assistancePlan: AssistancePlan?): WriteAccess {
        if (!accessService.canWriteEntries(assistancePlan?.institution?.id ?: 0)) {
            return WriteAccess.FORBIDDEN
        }
        if (assistancePlan?.client?.archived == true) {
            return WriteAccess.CLIENT_ARCHIVED
        }
        return WriteAccess.ALLOWED
    }

    private fun getCurrentEmployee(): Employee {
        return employeeService.getEntityById(accessService.getId())
            ?: throw IllegalStateException("current user not found")
    }

    private fun buildGoalEvaluationsYear(
        goal: Goal,
        year: Int,
        start: LocalDate,
        end: LocalDate,
        evaluations: List<Evaluation>
    ): GoalEvaluationsYearResponse {
        val months = (1..12).map { month ->
            val yearMonth = YearMonth.of(year, month)
            EvaluationMonthResponse(
                month = month,
                assistancePlanActive = DateService.isYearMonthInBetweenInclusive(yearMonth, start, end),
                evaluation = evaluations
                    .firstOrNull { YearMonth.of(it.date.year, it.date.monthValue) == yearMonth }
                    ?.let { EvaluationResponse.from(it) }
            )
        }

        return GoalEvaluationsYearResponse(goal.id, goal.title, months)
    }
}
