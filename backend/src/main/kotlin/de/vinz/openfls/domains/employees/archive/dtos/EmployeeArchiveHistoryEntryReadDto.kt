package de.vinz.openfls.domains.employees.archive.dtos

import de.vinz.openfls.domains.employees.archive.EmployeeArchiveActionType
import java.time.LocalDate
import java.time.LocalDateTime

class EmployeeArchiveHistoryEntryReadDto {
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
        fun from(dto: EmployeeArchiveHistoryEntryDto): EmployeeArchiveHistoryEntryReadDto {
            return EmployeeArchiveHistoryEntryReadDto().apply {
                id = dto.id
                actionType = dto.actionType
                actionDate = dto.actionDate
                actionTimestamp = dto.actionTimestamp
                reason = dto.reason
                remark = dto.remark
                executingEmployeeId = dto.executingEmployeeId
                executingEmployeeFirstname = dto.executingEmployeeFirstname
                executingEmployeeLastname = dto.executingEmployeeLastname
            }
        }
    }
}
