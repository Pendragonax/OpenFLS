package de.vinz.openfls.domains.employees.archive

import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.archive.dtos.EmployeeArchiveHistoryEntryDto
import de.vinz.openfls.domains.employees.archive.dtos.EmployeeArchiveHistoryEntryReadDto
import de.vinz.openfls.exceptions.UserNotAllowedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class EmployeeArchiveService(
    private val employeeRepository: EmployeeRepository
) {

    @Transactional
    @Throws(UserNotAllowedException::class, EmployeeArchiveStateException::class)
    fun archive(
        employeeId: Long,
        actionDate: LocalDate,
        reason: String,
        remark: String,
        actor: EmployeeArchiveActor
    ): EmployeeArchiveHistoryEntryDto {
        return changeArchiveState(
            employeeId = employeeId,
            actionType = EmployeeArchiveActionType.ARCHIVE,
            actionDate = actionDate,
            reason = reason,
            remark = remark,
            actor = actor
        )
    }

    @Transactional
    @Throws(UserNotAllowedException::class, EmployeeArchiveStateException::class)
    fun reactivate(
        employeeId: Long,
        actionDate: LocalDate,
        reason: String,
        remark: String,
        actor: EmployeeArchiveActor
    ): EmployeeArchiveHistoryEntryDto {
        return changeArchiveState(
            employeeId = employeeId,
            actionType = EmployeeArchiveActionType.REACTIVATE,
            actionDate = actionDate,
            reason = reason,
            remark = remark,
            actor = actor
        )
    }

    @Transactional(readOnly = true)
    fun getArchiveHistory(employeeId: Long): List<EmployeeArchiveHistoryEntryReadDto> {
        val employee = employeeRepository.findById(employeeId).orElse(null)
            ?: return emptyList()

        return employee.archiveHistoryEntries
            .sortedByDescending { it.actionTimestamp }
            .map { EmployeeArchiveHistoryEntryReadDto.from(EmployeeArchiveHistoryEntryDto.from(it)) }
    }

    private fun changeArchiveState(
        employeeId: Long,
        actionType: EmployeeArchiveActionType,
        actionDate: LocalDate,
        reason: String,
        remark: String,
        actor: EmployeeArchiveActor
    ): EmployeeArchiveHistoryEntryDto {
        if (!actor.isAdmin) {
            throw UserNotAllowedException()
        }

        val employee = employeeRepository.findById(employeeId)
            .orElseThrow { IllegalArgumentException("employee not found") }

        when (actionType) {
            EmployeeArchiveActionType.ARCHIVE -> {
                if (employee.archived) {
                    throw EmployeeArchiveStateException("employee already archived")
                }
            }
            EmployeeArchiveActionType.REACTIVATE -> {
                if (!employee.archived) {
                    throw EmployeeArchiveStateException("employee is not archived")
                }
            }
        }

        val actionTimestamp = LocalDateTime.now()
        val historyEntry = EmployeeArchiveHistoryEntry(
            actionType = actionType,
            actionDate = actionDate,
            actionTimestamp = actionTimestamp,
            reason = reason,
            remark = remark,
            executingEmployeeId = actor.employeeId,
            executingEmployeeFirstname = actor.firstname,
            executingEmployeeLastname = actor.lastname,
            employee = employee
        )

        employee.archived = actionType == EmployeeArchiveActionType.ARCHIVE
        employee.archiveHistoryEntries.add(historyEntry)
        employeeRepository.save(employee)

        return EmployeeArchiveHistoryEntryDto.from(historyEntry)
    }
}
