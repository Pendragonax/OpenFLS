package de.vinz.openfls.domains.employees.service

import de.vinz.openfls.domains.employees.dto.EmployeeArchiveHistoryEntryResponse
import de.vinz.openfls.domains.employees.dto.EmployeeArchiveResult
import de.vinz.openfls.domains.employees.entity.EmployeeArchiveActionType
import de.vinz.openfls.domains.employees.entity.EmployeeArchiveHistoryEntry
import de.vinz.openfls.domains.permissions.service.AccessService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class EmployeeArchiveService(
    private val employeeService: EmployeeService,
    private val accessService: AccessService
) {

    @Transactional
    fun archive(employeeId: Long, actionDate: LocalDate, reason: String, remark: String): EmployeeArchiveResult {
        return changeArchiveState(employeeId, EmployeeArchiveActionType.ARCHIVE, actionDate, reason, remark)
    }

    @Transactional
    fun reactivate(employeeId: Long, actionDate: LocalDate, reason: String, remark: String): EmployeeArchiveResult {
        return changeArchiveState(employeeId, EmployeeArchiveActionType.REACTIVATE, actionDate, reason, remark)
    }

    @Transactional(readOnly = true)
    fun getHistory(employeeId: Long): List<EmployeeArchiveHistoryEntryResponse>? {
        val employee = employeeService.getEntityById(employeeId) ?: return null

        return employee.archiveHistoryEntries
            .sortedByDescending { it.actionTimestamp }
            .map { EmployeeArchiveHistoryEntryResponse.from(it) }
    }

    private fun changeArchiveState(
        employeeId: Long,
        actionType: EmployeeArchiveActionType,
        actionDate: LocalDate,
        reason: String,
        remark: String
    ): EmployeeArchiveResult {
        val actor = employeeService.getEmployeeNameById(accessService.getId(), includeArchived = true)
            ?: return EmployeeArchiveResult.ActorNotFound
        val employee = employeeService.getEntityById(employeeId) ?: return EmployeeArchiveResult.NotFound

        when (actionType) {
            EmployeeArchiveActionType.ARCHIVE ->
                if (employee.archived) return EmployeeArchiveResult.AlreadyArchived
            EmployeeArchiveActionType.REACTIVATE ->
                if (!employee.archived) return EmployeeArchiveResult.NotArchived
        }

        val historyEntry = EmployeeArchiveHistoryEntry(
            actionType = actionType,
            actionDate = actionDate,
            actionTimestamp = LocalDateTime.now(),
            reason = reason,
            remark = remark,
            executingEmployeeId = actor.id,
            executingEmployeeFirstname = actor.firstName,
            executingEmployeeLastname = actor.lastName,
            employee = employee
        )

        employee.archived = actionType == EmployeeArchiveActionType.ARCHIVE
        employee.archiveHistoryEntries.add(historyEntry)
        employeeService.saveEntity(employee)

        return EmployeeArchiveResult.Success(EmployeeArchiveHistoryEntryResponse.from(historyEntry))
    }
}
