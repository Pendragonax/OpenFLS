package de.vinz.openfls.domains.clients.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.clients.dto.ClientArchiveHistoryEntryResponse
import de.vinz.openfls.domains.clients.dto.ClientArchiveHistoryResult
import de.vinz.openfls.domains.clients.dto.ClientArchiveResult
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.clients.entity.ClientArchiveActionType
import de.vinz.openfls.domains.clients.entity.ClientArchiveExportFormat
import de.vinz.openfls.domains.clients.entity.ClientArchiveHistoryEntry
import de.vinz.openfls.domains.employees.dto.EmployeeNameDto
import de.vinz.openfls.domains.employees.service.EmployeeFavoriteService
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.permissions.service.AccessService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Archiving, reactivating and the history of a client. Only admins and the leaders of the
 * client's institution may use it.
 */
@Service
class ClientArchiveService(
    private val clientService: ClientService,
    private val employeeService: EmployeeService,
    private val employeeFavoriteService: EmployeeFavoriteService,
    private val accessService: AccessService
) {

    @Transactional
    fun archive(clientId: Long, actionDate: LocalDate, reason: String, remark: String): ClientArchiveResult {
        return changeArchiveState(clientId, ClientArchiveActionType.ARCHIVE, actionDate, reason, remark)
    }

    @Transactional
    fun reactivate(clientId: Long, actionDate: LocalDate, reason: String, remark: String): ClientArchiveResult {
        return changeArchiveState(clientId, ClientArchiveActionType.REACTIVATE, actionDate, reason, remark)
    }

    @Transactional(readOnly = true)
    fun getHistory(clientId: Long): ClientArchiveHistoryResult {
        val client = clientService.getEntityById(clientId) ?: return ClientArchiveHistoryResult.NotFound
        if (!canManageArchive(client)) {
            return ClientArchiveHistoryResult.Forbidden
        }

        return ClientArchiveHistoryResult.Success(
            client.archiveHistoryEntries
                .sortedByDescending { it.actionTimestamp }
                .map { ClientArchiveHistoryEntryResponse.from(it) }
        )
    }

    @InternalEntityApi
    @Transactional
    fun recordExport(
        client: Client,
        actor: EmployeeNameDto,
        actionTimestamp: LocalDateTime,
        remark: String,
        exportFormat: ClientArchiveExportFormat
    ) {
        client.archiveHistoryEntries.add(
            ClientArchiveHistoryEntry(
                actionType = ClientArchiveActionType.EXPORT,
                exportFormat = exportFormat,
                actionDate = actionTimestamp.toLocalDate(),
                actionTimestamp = actionTimestamp,
                reason = EXPORT_REASON,
                remark = remark,
                executingEmployeeId = actor.id,
                executingEmployeeFirstname = actor.firstName,
                executingEmployeeLastname = actor.lastName,
                client = client
            )
        )
        clientService.saveEntity(client)
    }

    /** Admins and the leaders of the client's institution. */
    @InternalEntityApi
    fun canManageArchive(client: Client): Boolean {
        return accessService.isLeader(client.institution?.id ?: 0)
    }

    private fun changeArchiveState(
        clientId: Long,
        actionType: ClientArchiveActionType,
        actionDate: LocalDate,
        reason: String,
        remark: String
    ): ClientArchiveResult {
        val client = clientService.getEntityById(clientId) ?: return ClientArchiveResult.NotFound
        if (!canManageArchive(client)) {
            return ClientArchiveResult.Forbidden
        }
        val actor = employeeService.getEmployeeNameById(accessService.getId(), includeArchived = accessService.isAdmin())
            ?: return ClientArchiveResult.ActorNotFound

        when (actionType) {
            ClientArchiveActionType.ARCHIVE -> if (client.archived) return ClientArchiveResult.AlreadyArchived
            ClientArchiveActionType.REACTIVATE -> if (!client.archived) return ClientArchiveResult.NotArchived
            ClientArchiveActionType.EXPORT -> throw IllegalArgumentException("unsupported client archive action")
        }

        val historyEntry = ClientArchiveHistoryEntry(
            actionType = actionType,
            actionDate = actionDate,
            actionTimestamp = LocalDateTime.now(),
            reason = reason,
            remark = remark,
            executingEmployeeId = actor.id,
            executingEmployeeFirstname = actor.firstName,
            executingEmployeeLastname = actor.lastName,
            client = client
        )

        client.archived = actionType == ClientArchiveActionType.ARCHIVE
        client.archiveHistoryEntries.add(historyEntry)
        clientService.saveEntity(client)

        if (actionType == ClientArchiveActionType.ARCHIVE) {
            employeeFavoriteService.deleteAssistancePlanFavoritesByClientId(clientId)
            employeeFavoriteService.deleteClientFavoritesByClientId(clientId)
        }

        return ClientArchiveResult.Success(ClientArchiveHistoryEntryResponse.from(historyEntry))
    }

    private companion object {
        const val EXPORT_REASON = "Export requested"
    }
}
