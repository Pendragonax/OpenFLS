package de.vinz.openfls.domains.employees.dto

import de.vinz.openfls.domains.employees.entity.EmployeeArchiveActionType
import de.vinz.openfls.domains.employees.entity.EmployeeArchiveHistoryEntry
import java.time.LocalDate
import java.time.LocalDateTime

data class EmployeeArchiveHistoryEntryResponse(
    val id: Long = 0,
    val actionType: EmployeeArchiveActionType = EmployeeArchiveActionType.ARCHIVE,
    val actionDate: LocalDate = LocalDate.now(),
    val actionTimestamp: LocalDateTime = LocalDateTime.now(),
    val reason: String = "",
    val remark: String = "",
    val executingEmployeeId: Long = 0,
    val executingEmployeeFirstname: String = "",
    val executingEmployeeLastname: String = ""
) {
    companion object {
        fun from(entry: EmployeeArchiveHistoryEntry): EmployeeArchiveHistoryEntryResponse {
            return EmployeeArchiveHistoryEntryResponse(
                id = entry.id,
                actionType = entry.actionType,
                actionDate = entry.actionDate,
                actionTimestamp = entry.actionTimestamp,
                reason = entry.reason,
                remark = entry.remark,
                executingEmployeeId = entry.executingEmployeeId,
                executingEmployeeFirstname = entry.executingEmployeeFirstname,
                executingEmployeeLastname = entry.executingEmployeeLastname
            )
        }
    }
}
