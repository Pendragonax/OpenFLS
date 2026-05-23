package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanForServiceEditingDto
import de.vinz.openfls.domains.categories.CategoryTemplateService
import de.vinz.openfls.domains.clients.archive.ClientArchiveActionType
import de.vinz.openfls.domains.clients.archive.ClientArchiveHistoryEntry
import de.vinz.openfls.domains.clients.archive.ClientArchiveStateException
import de.vinz.openfls.domains.clients.archive.dtos.ClientArchiveHistoryEntryDto
import de.vinz.openfls.domains.clients.dtos.ClientDto
import de.vinz.openfls.domains.clients.dtos.ClientForServiceEditingDto
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto
import de.vinz.openfls.domains.clients.dtos.ClientSoloDto
import de.vinz.openfls.domains.hourTypes.HourTypeDto
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.services.GenericService
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
) : GenericService<Client> {

    @Transactional
    fun create(value: ClientDto): ClientDto {
        val clientEntity = modelMapper.map(value, Client::class.java)
        val resultClientEntity = create(clientEntity)
        val clientDto = modelMapper.map(resultClientEntity, ClientDto::class.java)
        return sortClientDto(clientDto, resultClientEntity)
    }

    @Transactional
    override fun create(value: Client): Client {
        value.institution = institutionService.getEntityById(value.institution?.id ?: 0)
                ?: throw IllegalArgumentException("institution not found")
        value.categoryTemplate = categoryTemplateService.getById(value.categoryTemplate?.id ?: 0)
                ?: throw IllegalArgumentException("category template not found")
        value.archived = false

        return clientRepository.save(value)
    }

    @Transactional
    fun update(value: ClientDto): ClientDto {
        val clientEntity = modelMapper.map(value, Client::class.java)
        val resultClientEntity = update(clientEntity)
        val clientDto = modelMapper.map(resultClientEntity, ClientDto::class.java)
        return sortClientDto(clientDto, resultClientEntity)
    }

    @Transactional
    @Throws(ClientArchiveStateException::class)
    override fun update(value: Client): Client {
        val existingClient = clientRepository.findById(value.id)
                .orElseThrow { IllegalArgumentException("client not found") }

        if (existingClient.archived) {
            throw ClientArchiveStateException("client is archived")
        }

        existingClient.firstName = value.firstName
        existingClient.lastName = value.lastName
        existingClient.phoneNumber = value.phoneNumber
        existingClient.email = value.email
        existingClient.institution = institutionService.getEntityById(value.institution?.id ?: 0)
                ?: throw IllegalArgumentException("institution not found")
        existingClient.categoryTemplate = categoryTemplateService.getById(value.categoryTemplate?.id ?: 0)
                ?: throw IllegalArgumentException("category template not found")

        return clientRepository.save(existingClient)
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
        val client = getById(clientId) ?: return emptyList()
        return client.archiveHistoryEntries
            .sortedByDescending { it.actionTimestamp }
            .map { ClientArchiveHistoryEntryDto.from(it) }
    }

    @Transactional
    @Throws(ClientArchiveStateException::class)
    override fun delete(id: Long) {
        val client = getById(id) ?: throw IllegalArgumentException("client not found")

        if (client.archived) {
            throw ClientArchiveStateException("client is archived")
        }

        clientRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    override fun getAll(): List<Client> {
        return clientRepository.findAll().toList()
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

    @Transactional(readOnly = true)
    fun getDtoById(
        id: Long,
        includeArchived: Boolean = false,
        leadingInstitutionIds: List<Long> = emptyList()
    ): ClientDto? {
        val entity = getById(id)

        if (entity != null && isVisible(entity.archived, entity.institution?.id, includeArchived, leadingInstitutionIds)) {
            val clientDto = modelMapper.map(entity, ClientDto::class.java)
            return sortClientDto(clientDto, entity)
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
        val entity = getById(clientId)

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
    override fun getById(id: Long): Client? {
        return clientRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    override fun existsById(id: Long): Boolean {
        return clientRepository.existsById(id)
    }

    @Transactional(readOnly = true)
    fun existById(id: Long): Boolean {
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
        val client = getById(clientId) ?: throw IllegalArgumentException("client not found")

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

    private fun sortClientDto(clientDto: ClientDto, entity: Client): ClientDto {
        val institutionNamesByAssistancePlanId = entity.assistancePlans.associate { it.id to (it.institution?.name ?: "") }
        clientDto.assistancePlans.forEach { plan ->
            plan.institutionName = institutionNamesByAssistancePlanId[plan.id] ?: ""
        }
        clientDto.assistancePlans =
                clientDto.assistancePlans.sortedBy { it.start }.toTypedArray()
        clientDto.categoryTemplate.categories =
                clientDto.categoryTemplate.categories.sortedBy { it.shortcut }
        return clientDto
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
        planDto.possibleDocumentationHourTypes = extractPossibleDocumentationHourTypes(plan)
        return planDto
    }

    private fun extractPossibleDocumentationHourTypes(plan: AssistancePlan): Array<HourTypeDto> {
        return (plan.hours.mapNotNull { it.hourType } + plan.goals.flatMap { it.hours.mapNotNull { hour -> hour.hourType } })
            .distinctBy { it.id }
            .sortedBy { it.title.lowercase() }
            .map { HourTypeDto.from(it) }
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
