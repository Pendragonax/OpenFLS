package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanForServiceEditingDto
import de.vinz.openfls.domains.categories.CategoryTemplateService
import de.vinz.openfls.domains.clients.archive.ClientArchiveActionType
import de.vinz.openfls.domains.clients.archive.ClientArchiveHistoryEntry
import de.vinz.openfls.domains.clients.archive.ClientArchiveStateException
import de.vinz.openfls.domains.clients.archive.dtos.ClientArchiveHistoryEntryDto
import de.vinz.openfls.domains.clients.dtos.ClientCreateDto
import de.vinz.openfls.domains.clients.dtos.ClientDto
import de.vinz.openfls.domains.clients.dtos.ClientFavoriteRowDto
import de.vinz.openfls.domains.clients.dtos.ClientForServiceEditingDto
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto
import de.vinz.openfls.domains.clients.dtos.ClientSoloDto
import de.vinz.openfls.domains.clients.dtos.ClientUpdateDto
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeResponse
import de.vinz.openfls.domains.institutions.InstitutionService

import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ClientService(
        private val clientRepository: ClientRepository,
        private val institutionService: InstitutionService,
        private val categoryTemplateService: CategoryTemplateService,
        private val modelMapper: ModelMapper
) {

    @Transactional
    fun create(valueDto: ClientCreateDto): ClientDto {
        val entity = Client(
                firstName = valueDto.firstName,
                lastName = valueDto.lastName,
                phoneNumber = valueDto.phoneNumber,
                email = valueDto.email,
                archived = false
        )
        entity.institution = institutionService.getEntityById(valueDto.institutionId)
                ?: throw IllegalArgumentException("institution not found")
        entity.categoryTemplate = categoryTemplateService.getEntityById(valueDto.categoryTemplateId)
                ?: throw IllegalArgumentException("category template not found")

        val savedEntity = clientRepository.save(entity)
        return toClientDto(savedEntity)
    }

    @Transactional
    @Throws(ClientArchiveStateException::class)
    fun update(valueDto: ClientUpdateDto): ClientDto {
        val existingClient = clientRepository.findById(valueDto.id)
                .orElseThrow { IllegalArgumentException("client not found") }

        if (existingClient.archived) {
            throw ClientArchiveStateException("client is archived")
        }

        existingClient.firstName = valueDto.firstName
        existingClient.lastName = valueDto.lastName
        existingClient.phoneNumber = valueDto.phoneNumber
        existingClient.email = valueDto.email
        existingClient.institution = institutionService.getEntityById(valueDto.institutionId)
                ?: throw IllegalArgumentException("institution not found")
        existingClient.categoryTemplate = categoryTemplateService.getEntityById(valueDto.categoryTemplateId)
                ?: throw IllegalArgumentException("category template not found")

        val savedEntity = clientRepository.save(existingClient)
        return toClientDto(savedEntity)
    }

    @Transactional
    @Throws(ClientArchiveStateException::class)
    fun archive(
        clientId: Long,
        actionDate: LocalDate,
        actionTimestamp: LocalDateTime,
        executingEmployeeId: Long,
        executingEmployeeFirstname: String,
        executingEmployeeLastname: String,
        reason: String,
        remark: String
    ): ClientArchiveHistoryEntryDto {
        return ClientArchiveHistoryEntryDto.from(
            changeArchiveState(
                clientId = clientId,
                actionType = ClientArchiveActionType.ARCHIVE,
                actionDate = actionDate,
                actionTimestamp = actionTimestamp,
                executingEmployeeId = executingEmployeeId,
                executingEmployeeFirstname = executingEmployeeFirstname,
                executingEmployeeLastname = executingEmployeeLastname,
                reason = reason,
                remark = remark
            )
        )
    }

    @Transactional
    @Throws(ClientArchiveStateException::class)
    fun reactivate(
        clientId: Long,
        actionDate: LocalDate,
        actionTimestamp: LocalDateTime,
        executingEmployeeId: Long,
        executingEmployeeFirstname: String,
        executingEmployeeLastname: String,
        reason: String,
        remark: String
    ): ClientArchiveHistoryEntryDto {
        return ClientArchiveHistoryEntryDto.from(
            changeArchiveState(
                clientId = clientId,
                actionType = ClientArchiveActionType.REACTIVATE,
                actionDate = actionDate,
                actionTimestamp = actionTimestamp,
                executingEmployeeId = executingEmployeeId,
                executingEmployeeFirstname = executingEmployeeFirstname,
                executingEmployeeLastname = executingEmployeeLastname,
                reason = reason,
                remark = remark
            )
        )
    }

    @Transactional(readOnly = true)
    fun getArchiveHistoryById(clientId: Long): List<ClientArchiveHistoryEntryDto> {
        val client = getEntityById(clientId) ?: return emptyList()
        return client.archiveHistoryEntries
            .sortedByDescending { it.actionTimestamp }
            .map { ClientArchiveHistoryEntryDto.from(it) }
    }

    @Transactional
    @Throws(ClientArchiveStateException::class)
    fun delete(id: Long) {
        val client = getEntityById(id) ?: throw IllegalArgumentException("client not found")

        if (client.archived) {
            throw ClientArchiveStateException("client is archived")
        }

        clientRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAllClientSimpleDto(
        includeArchived: Boolean = false,
        leadingInstitutionIds: List<Long> = emptyList()
    ): List<ClientSimpleDto> {
        return clientRepository.findAll()
                .toList()
                .filter { includeArchived || !it.archived || leadingInstitutionIds.contains(it.institution?.id ?: 0) }
                .map { modelMapper.map(it, ClientSimpleDto::class.java) }
                .sortedBy { it.lastName.lowercase() }
    }

    @Transactional(readOnly = true)
    fun getAllClientSoloDto(
        includeArchived: Boolean = false,
        leadingInstitutionIds: List<Long> = emptyList()
    ): List<ClientSoloDto> {
        return clientRepository.findAll()
                .toList()
                .filter { includeArchived || !it.archived || leadingInstitutionIds.contains(it.institution?.id ?: 0) }
                .map { modelMapper.map(it, ClientSoloDto::class.java) }
                .sortedBy { it.lastName.lowercase() }
    }

    /**
     * Base data of the clients an employee marked as favourite.
     */
    @Transactional(readOnly = true)
    fun getFavoriteRowDtosByEmployeeId(employeeId: Long): List<ClientFavoriteRowDto> {
        return clientRepository.findFavoriteRowDtosByEmployeeId(employeeId)
    }

    @Transactional(readOnly = true)
    fun isFavoriteOfEmployee(clientId: Long, employeeId: Long): Boolean {
        return clientRepository.findFavoriteClientIdsByEmployeeId(employeeId).contains(clientId)
    }

    @Transactional(readOnly = true)
    fun getById(
        id: Long,
        includeArchived: Boolean = false,
        leadingInstitutionIds: List<Long> = emptyList()
    ): ClientDto? {
        val entity = getEntityById(id)

        if (entity != null && isVisible(entity.archived, entity.institution?.id, includeArchived, leadingInstitutionIds)) {
            return toClientDto(entity)
        }

        return null
    }

    @Transactional(readOnly = true)
    fun getForServiceEditingById(
        clientId: Long,
        allowedInstitutions: List<Long>,
        includeArchived: Boolean = false,
        leadingInstitutionIds: List<Long> = emptyList()
    ): ClientForServiceEditingDto? {
        val entity = getEntityById(clientId)

        if (entity != null && isVisible(entity.archived, entity.institution?.id, includeArchived, leadingInstitutionIds)) {
            val clientDto = modelMapper.map(entity, ClientForServiceEditingDto::class.java)
            clientDto.assistancePlans = entity.assistancePlans
                .filter { assistancePlan -> allowedInstitutions.any { it == assistancePlan.institution?.id } }
                .map { mapToServiceEditingAssistancePlanDto(it, entity.id) }
                .sortedBy { it.start }
                .toTypedArray()
            clientDto.categoryTemplate.categories = clientDto.categoryTemplate.categories.sortedBy { it.shortcut }
            return clientDto
        }

        return null
    }

    @Transactional(readOnly = true)
    fun getEntityById(id: Long): Client? {
        return clientRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return clientRepository.existsById(id)
    }

    private fun changeArchiveState(
        clientId: Long,
        actionType: ClientArchiveActionType,
        actionDate: LocalDate,
        actionTimestamp: LocalDateTime,
        executingEmployeeId: Long,
        executingEmployeeFirstname: String,
        executingEmployeeLastname: String,
        reason: String,
        remark: String
    ): ClientArchiveHistoryEntry {
        val client = getEntityById(clientId) ?: throw IllegalArgumentException("client not found")

        when (actionType) {
            ClientArchiveActionType.ARCHIVE -> {
                if (client.archived) {
                    throw ClientArchiveStateException("client already archived")
                }
            }
            ClientArchiveActionType.REACTIVATE -> {
                if (!client.archived) {
                    throw ClientArchiveStateException("client is not archived")
                }
            }
            ClientArchiveActionType.EXPORT -> {
                throw IllegalArgumentException("unsupported client archive action")
            }
        }

        val historyEntry = ClientArchiveHistoryEntry(
                actionType = actionType,
                actionDate = actionDate,
                actionTimestamp = actionTimestamp,
                reason = reason,
                remark = remark,
                executingEmployeeId = executingEmployeeId,
                executingEmployeeFirstname = executingEmployeeFirstname,
                executingEmployeeLastname = executingEmployeeLastname,
                client = client
        )

        client.archived = actionType == ClientArchiveActionType.ARCHIVE
        client.archiveHistoryEntries.add(historyEntry)
        clientRepository.save(client)
        return historyEntry
    }

    private fun toClientDto(entity: Client): ClientDto {
        val dto = modelMapper.map(entity, ClientDto::class.java)
        dto.categoryTemplateId = entity.categoryTemplate?.id ?: 0
        dto.categoryTemplateTitle = entity.categoryTemplate?.title ?: ""
        return dto
    }

    private fun mapToServiceEditingAssistancePlanDto(
        plan: AssistancePlan,
        clientId: Long
    ): AssistancePlanForServiceEditingDto {
        val planDto = modelMapper.map(plan, AssistancePlanForServiceEditingDto::class.java)
        planDto.clientId = clientId
        planDto.institutionId = plan.institution?.id ?: 0
        planDto.institutionName = plan.institution?.name ?: ""
        planDto.sponsorId = plan.sponsor?.id ?: 0
        planDto.hourMode = plan.hourMode
        planDto.hourCorridorId = plan.hourCorridor?.id ?: 0
        planDto.possibleDocumentationHourTypes = extractPossibleDocumentationHourTypes(plan)
        return planDto
    }

    private fun extractPossibleDocumentationHourTypes(plan: AssistancePlan): Array<HourTypeResponse> {
        return (plan.hours.mapNotNull { it.hourType } + plan.goals.flatMap { it.hours.mapNotNull { hour -> hour.hourType } })
            .distinctBy { it.id }
            .sortedBy { it.title.lowercase() }
            .map { HourTypeResponse.from(it) }
            .toTypedArray()
    }

    private fun isVisible(
        archived: Boolean,
        institutionId: Long?,
        includeArchived: Boolean,
        leadingInstitutionIds: List<Long>
    ): Boolean {
        return !archived || includeArchived || leadingInstitutionIds.contains(institutionId ?: 0)
    }
}
