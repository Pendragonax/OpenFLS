package de.vinz.openfls.domains.clients.archive

import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.clients.archive.dtos.ClientArchiveHistoryEntryDto
import de.vinz.openfls.domains.clients.archive.dtos.ClientArchiveHistoryEntryReadDto
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.exceptions.UserNotAllowedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ClientArchiveService(
    private val clientService: ClientService,
    private val employeeService: EmployeeService
) {

    @Transactional
    @Throws(UserNotAllowedException::class, ClientArchiveStateException::class)
    fun archive(
        clientId: Long,
        actionDate: LocalDate,
        reason: String,
        remark: String,
        actor: ClientArchiveActor
    ): ClientArchiveHistoryEntryDto {
        return changeArchiveState(
            clientId = clientId,
            actionType = ClientArchiveActionType.ARCHIVE,
            actionDate = actionDate,
            reason = reason,
            remark = remark,
            actor = actor
        )
    }

    @Transactional
    @Throws(UserNotAllowedException::class, ClientArchiveStateException::class)
    fun reactivate(
        clientId: Long,
        actionDate: LocalDate,
        reason: String,
        remark: String,
        actor: ClientArchiveActor
    ): ClientArchiveHistoryEntryDto {
        return changeArchiveState(
            clientId = clientId,
            actionType = ClientArchiveActionType.REACTIVATE,
            actionDate = actionDate,
            reason = reason,
            remark = remark,
            actor = actor
        )
    }

    @Transactional(readOnly = true)
    fun getArchiveHistory(clientId: Long): List<ClientArchiveHistoryEntryReadDto> {
        return clientService.getArchiveHistoryById(clientId)
            .map { ClientArchiveHistoryEntryReadDto.from(it) }
    }

    private fun changeArchiveState(
        clientId: Long,
        actionType: ClientArchiveActionType,
        actionDate: LocalDate,
        reason: String,
        remark: String,
        actor: ClientArchiveActor
    ): ClientArchiveHistoryEntryDto {
        val client = clientService.getById(clientId)
            ?: throw IllegalArgumentException("client not found")

        if (!actor.isAdmin && !actor.leadingInstitutionIds.contains(client.institution?.id ?: 0)) {
            throw UserNotAllowedException()
        }

        val actionTimestamp = LocalDateTime.now()

        return when (actionType) {
            ClientArchiveActionType.ARCHIVE -> clientService.archive(
                clientId = clientId,
                actionDate = actionDate,
                actionTimestamp = actionTimestamp,
                executingEmployeeId = actor.employeeId,
                executingEmployeeFirstname = actor.firstname,
                executingEmployeeLastname = actor.lastname,
                reason = reason,
                remark = remark
            ).also {
                employeeService.deleteAssistancePlanFavoritesByClientId(clientId)
            }
            ClientArchiveActionType.REACTIVATE -> clientService.reactivate(
                clientId = clientId,
                actionDate = actionDate,
                actionTimestamp = actionTimestamp,
                executingEmployeeId = actor.employeeId,
                executingEmployeeFirstname = actor.firstname,
                executingEmployeeLastname = actor.lastname,
                reason = reason,
                remark = remark
            )
        }
    }
}
