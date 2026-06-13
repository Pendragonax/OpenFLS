package de.vinz.openfls.domains.clients.archive.dtos

import de.vinz.openfls.domains.clients.archive.ClientArchiveActionType
import de.vinz.openfls.domains.clients.archive.ClientArchiveHistoryEntry
import de.vinz.openfls.domains.clients.archive.export.ClientArchiveExportFormat
import java.time.LocalDate
import java.time.LocalDateTime

class ClientArchiveHistoryEntryDto {
    var id: Long = 0
    var actionType: ClientArchiveActionType = ClientArchiveActionType.ARCHIVE
    var exportFormat: ClientArchiveExportFormat? = null
    var actionDate: LocalDate = LocalDate.now()
    var actionTimestamp: LocalDateTime = LocalDateTime.now()
    var reason: String = ""
    var remark: String = ""
    var executingEmployeeId: Long = 0
    var executingEmployeeFirstname: String = ""
    var executingEmployeeLastname: String = ""

    companion object {
        fun from(entity: ClientArchiveHistoryEntry): ClientArchiveHistoryEntryDto {
            return ClientArchiveHistoryEntryDto().apply {
                id = entity.id
                actionType = entity.actionType
                exportFormat = entity.exportFormat
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
