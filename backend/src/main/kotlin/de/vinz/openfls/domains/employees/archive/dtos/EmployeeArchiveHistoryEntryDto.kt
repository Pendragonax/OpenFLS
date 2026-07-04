package de.vinz.openfls.domains.employees.archive.dtos

import de.vinz.openfls.domains.employees.archive.EmployeeArchiveActionType
import de.vinz.openfls.domains.employees.archive.EmployeeArchiveHistoryEntry
import java.time.LocalDate
import java.time.LocalDateTime

class EmployeeArchiveHistoryEntryDto {
    var id: Long = 0
    var actionType: EmployeeArchiveActionType = EmployeeArchiveActionType.ARCHIVE
    var actionDate: LocalDate = LocalDate.now()
    var actionTimestamp: LocalDateTime = LocalDateTime.now()
    var reason: String = ""
    var remark: String = ""
    var executingEmployeeId: Long = 0
    var executingEmployeeFirstname: String = ""
    var executingEmployeeLastname: String = ""

    companion object {
        fun from(entity: EmployeeArchiveHistoryEntry): EmployeeArchiveHistoryEntryDto {
            return EmployeeArchiveHistoryEntryDto().apply {
                id = entity.id
                actionType = entity.actionType
                actionDate = entity.actionDate
                actionTimestamp = entity.actionTimestamp
                reason = entity.reason
                remark = entity.remark
                executingEmployeeId = entity.executingEmployeeId
                executingEmployeeFirstname = entity.executingEmployeeFirstname
                executingEmployeeLastname = entity.executingEmployeeLastname
            }
        }
    }
}
