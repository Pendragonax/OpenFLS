package de.vinz.openfls.domains.hourCorridors.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorAssistancePlanResponse
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorAuditLogResponse
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorCreateRequest
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorCreateResult
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorDeleteResult
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorResponse
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorUpdateRequest
import de.vinz.openfls.domains.hourCorridors.HourCorridorAuditAction
import de.vinz.openfls.domains.hourCorridors.repository.HourCorridorAuditLogRepository
import de.vinz.openfls.domains.hourCorridors.repository.HourCorridorRepository
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorUpdateResult
import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor
import de.vinz.openfls.domains.hourCorridors.entity.HourCorridorAuditLog
import de.vinz.openfls.domains.hourTypes.service.HourTypeService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class HourCorridorService(
    private val hourCorridorRepository: HourCorridorRepository,
    private val hourTypeService: HourTypeService,
    private val auditLogRepository: HourCorridorAuditLogRepository,
    private val clock: Clock
) {

    @Transactional
    fun create(request: HourCorridorCreateRequest): HourCorridorCreateResult {
        if (request.weeklyMinutesTill < request.weeklyMinutesFrom) {
            return HourCorridorCreateResult.InvalidRange("till before from")
        }

        val hourType = hourTypeService.getEntityById(request.hourTypeId)
            ?: return HourCorridorCreateResult.HourTypeNotFound("hour type with id ${request.hourTypeId} not found")

        val entity = hourCorridorRepository.save(
            HourCorridor(
                title = request.title,
                weeklyMinutesFrom = request.weeklyMinutesFrom,
                weeklyMinutesTill = request.weeklyMinutesTill,
                hourType = hourType
            )
        )
        writeAudit(entity.id, HourCorridorAuditAction.CREATE, null, AuditSnapshot.from(entity))
        return HourCorridorCreateResult.Success(
            HourCorridorResponse.from(entity, countAssistancePlansByHourCorridorId(entity.id))
        )
    }

    @Transactional
    fun update(request: HourCorridorUpdateRequest): HourCorridorUpdateResult {
        if (request.weeklyMinutesTill < request.weeklyMinutesFrom) {
            return HourCorridorUpdateResult.InvalidRange("till before from")
        }

        val entity = hourCorridorRepository.findByIdOrNull(request.id)
            ?: return HourCorridorUpdateResult.NotFound
        val hourType = hourTypeService.getEntityById(request.hourTypeId)
            ?: return HourCorridorUpdateResult.HourTypeNotFound("hour type with id ${request.hourTypeId} not found")
        val beforeSnapshot = AuditSnapshot.from(entity)

        entity.title = request.title
        entity.weeklyMinutesFrom = request.weeklyMinutesFrom
        entity.weeklyMinutesTill = request.weeklyMinutesTill
        entity.hourType = hourType

        val saved = hourCorridorRepository.save(entity)
        writeAudit(saved.id, HourCorridorAuditAction.UPDATE, beforeSnapshot, AuditSnapshot.from(saved))
        return HourCorridorUpdateResult.Success(
            HourCorridorResponse.from(saved, countAssistancePlansByHourCorridorId(saved.id))
        )
    }

    @Transactional
    fun delete(id: Long): HourCorridorDeleteResult {
        val before = hourCorridorRepository.findByIdOrNull(id)
            ?: return HourCorridorDeleteResult.NotFound
        val usageCount = countAssistancePlansByHourCorridorId(id)
        if (usageCount > 0) {
            return HourCorridorDeleteResult.Conflict(usageCount)
        }

        val beforeSnapshot = AuditSnapshot.from(before)
        hourCorridorRepository.deleteById(id)
        writeAudit(id, HourCorridorAuditAction.DELETE, beforeSnapshot, null)
        return HourCorridorDeleteResult.Success(HourCorridorResponse.from(before, 0))
    }

    @Transactional(readOnly = true)
    fun getAll(): List<HourCorridorResponse> {
        val entities = hourCorridorRepository.findAll()
            .toList()
            .sortedBy { it.title.lowercase() }
        val assistancePlanCounts = countAssistancePlansByHourCorridorIds(entities.map { it.id })
        return entities.map { HourCorridorResponse.from(it, assistancePlanCounts[it.id] ?: 0L) }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): HourCorridorResponse? {
        val entity = hourCorridorRepository.findByIdOrNull(id) ?: return null
        return HourCorridorResponse.from(entity, countAssistancePlansByHourCorridorId(entity.id))
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): HourCorridor? {
        return hourCorridorRepository.findByIdOrNull(id)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByIds(ids: List<Long>): List<HourCorridor> {
        return hourCorridorRepository.findAllById(ids).toList()
    }

    @Transactional(readOnly = true)
    fun countAssistancePlansByHourCorridorId(id: Long): Long {
        return hourCorridorRepository.countAssistancePlansByHourCorridorId(id)
    }

    @Transactional(readOnly = true)
    fun getAssistancePlansByHourCorridorId(id: Long): List<HourCorridorAssistancePlanResponse> =
        hourCorridorRepository.findAssistancePlansByHourCorridorId(id).map(HourCorridorAssistancePlanResponse::from)

    @Transactional(readOnly = true)
    fun getAuditHistoryByHourCorridorId(id: Long): List<HourCorridorAuditLogResponse> =
        auditLogRepository.findAllByHourCorridorIdOrderByChangedAtDescIdDesc(id).map(HourCorridorAuditLogResponse::from)

    private fun writeAudit(id: Long, action: HourCorridorAuditAction, before: AuditSnapshot?, after: AuditSnapshot?) {
        val authentication = SecurityContextHolder.getContext().authentication
        val actor = authentication?.takeIf { it.isAuthenticated && it.name != "anonymousUser" }?.name ?: "system"
        auditLogRepository.save(HourCorridorAuditLog(
            hourCorridorId = id,
            action = action,
            changedAt = LocalDateTime.now(clock),
            actor = actor,
            beforeTitle = before?.title,
            afterTitle = after?.title,
            beforeWeeklyMinutesFrom = before?.weeklyMinutesFrom,
            afterWeeklyMinutesFrom = after?.weeklyMinutesFrom,
            beforeWeeklyMinutesTill = before?.weeklyMinutesTill,
            afterWeeklyMinutesTill = after?.weeklyMinutesTill,
            beforeHourTypeId = before?.hourTypeId,
            afterHourTypeId = after?.hourTypeId
        ))
    }

    private data class AuditSnapshot(
        val title: String,
        val weeklyMinutesFrom: Int,
        val weeklyMinutesTill: Int,
        val hourTypeId: Long?
    ) {
        companion object {
            fun from(entity: HourCorridor) = AuditSnapshot(
                entity.title, entity.weeklyMinutesFrom, entity.weeklyMinutesTill, entity.hourType?.id
            )
        }
    }

    private fun countAssistancePlansByHourCorridorIds(ids: List<Long>): Map<Long, Long> {
        if (ids.isEmpty()) {
            return emptyMap()
        }
        return hourCorridorRepository.countAssistancePlansByHourCorridorIds(ids)
            .associate { it.hourCorridorId to it.assistancePlanCount }
    }
}
