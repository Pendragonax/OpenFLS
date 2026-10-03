package de.vinz.openfls.domains.clients.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportDownloadDto
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportDownloadLinkResponse
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportDownloadResult
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportRequestResult
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportStatusResponse
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportStatusResult
import de.vinz.openfls.domains.clients.dto.export.ClientArchiveExportDto
import de.vinz.openfls.domains.clients.entity.ClientArchiveExportFormat
import de.vinz.openfls.domains.clients.entity.ClientArchiveExportRecord
import de.vinz.openfls.domains.clients.repository.ClientArchiveExportRecordRepository
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.services.service.ServiceService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Service
class ClientArchiveExportService(
    private val clientService: ClientService,
    private val clientArchiveService: ClientArchiveService,
    private val serviceService: ServiceService,
    private val employeeService: EmployeeService,
    private val accessService: AccessService,
    private val assistancePlanService: AssistancePlanService,
    private val clientArchiveExportRecordRepository: ClientArchiveExportRecordRepository,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
    private val clientArchiveExportStorage: ClientArchiveExportStorage,
    @param:Value("\${openfls.client-archive-export.download-link-ttl:20m}")
    private val exportDownloadLinkTtl: Duration
) {

    @Transactional
    fun requestExport(
        clientId: Long,
        format: ClientArchiveExportFormat,
        anonymize: Boolean
    ): ClientArchiveExportRequestResult {
        if (format != ClientArchiveExportFormat.JSON) {
            return ClientArchiveExportRequestResult.UnsupportedFormat
        }

        val client = clientService.getEntityById(clientId) ?: return ClientArchiveExportRequestResult.NotFound
        if (!clientArchiveService.canManageArchive(client)) {
            return ClientArchiveExportRequestResult.Forbidden
        }
        val actor = employeeService.getEmployeeNameById(accessService.getId(), includeArchived = accessService.isAdmin())
            ?: return ClientArchiveExportRequestResult.ActorNotFound
        val now = LocalDateTime.now(clock)

        val exportData = ClientArchiveExportDto.from(
            client = client,
            services = serviceService.getAllEntitiesByClientId(clientId),
            assistancePlans = assistancePlanService.getAllEntitiesByClientId(clientId),
            anonymize = anonymize
        )
        val downloadToken = UUID.randomUUID().toString()
        val fileName = "client-$clientId-archive-export-$downloadToken.json"

        val exportFile = try {
            clientArchiveExportStorage.writeExport(
                clientId = clientId,
                fileName = fileName,
                content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(exportData)
            )
        } catch (ex: Exception) {
            clientArchiveExportStorage.delete(clientId, fileName)
            throw ex
        }

        try {
            val record = clientArchiveExportRecordRepository.save(
                ClientArchiveExportRecord(
                    downloadToken = downloadToken,
                    exportFormat = format,
                    requestedAt = now,
                    expiresAt = now.plus(exportDownloadLinkTtl),
                    fileName = fileName,
                    filePath = exportFile.toString(),
                    requestedByEmployeeId = actor.id,
                    requestedByEmployeeFirstname = actor.firstName,
                    requestedByEmployeeLastname = actor.lastName,
                    client = client
                )
            )

            clientArchiveService.recordExport(
                client = client,
                actor = actor,
                actionTimestamp = now,
                remark = if (anonymize) "${format.name} [anonym]" else format.name,
                exportFormat = format
            )

            return ClientArchiveExportRequestResult.Success(toStatusResponse(record, clientId))
        } catch (ex: Exception) {
            clientArchiveExportStorage.delete(exportFile.toString())
            throw ex
        }
    }

    @Transactional(readOnly = true)
    fun getExportStatus(clientId: Long): ClientArchiveExportStatusResult {
        val client = clientService.getEntityById(clientId) ?: return ClientArchiveExportStatusResult.NotFound
        if (!clientArchiveService.canManageArchive(client)) {
            return ClientArchiveExportStatusResult.Forbidden
        }
        val record = getActiveExport(clientId)
            ?: return ClientArchiveExportStatusResult.Success(ClientArchiveExportStatusResponse())

        return ClientArchiveExportStatusResult.Success(toStatusResponse(record, clientId))
    }

    @Transactional
    fun downloadExport(clientId: Long, downloadToken: String): ClientArchiveExportDownloadResult {
        val record = clientArchiveExportRecordRepository.findByClientIdAndDownloadToken(clientId, downloadToken)
            ?: return ClientArchiveExportDownloadResult.Gone("export not found")

        val now = LocalDateTime.now(clock)
        if (record.downloadedAt != null || !record.expiresAt.isAfter(now)) {
            cleanupExport(record)
            return ClientArchiveExportDownloadResult.Gone("export unavailable")
        }

        if (!clientArchiveExportStorage.exists(record.filePath)) {
            cleanupExport(record)
            return ClientArchiveExportDownloadResult.Gone("export file missing")
        }

        val content = clientArchiveExportStorage.read(record.filePath)
        clientArchiveExportStorage.delete(record.filePath)
        clientArchiveExportRecordRepository.delete(record)

        return ClientArchiveExportDownloadResult.Success(
            ClientArchiveExportDownloadDto(fileName = record.fileName, content = content)
        )
    }

    private fun getActiveExport(clientId: Long): ClientArchiveExportRecord? {
        val now = LocalDateTime.now(clock)
        return clientArchiveExportRecordRepository.findAllByClientId(clientId)
            .sortedByDescending { it.requestedAt }
            .firstOrNull { it.downloadedAt == null && it.expiresAt.isAfter(now) && clientArchiveExportStorage.exists(it.filePath) }
            ?: run {
                cleanupExpiredExports(clientId, now)
                null
            }
    }

    private fun cleanupExpiredExports(clientId: Long, now: LocalDateTime) {
        clientArchiveExportRecordRepository.findAllByClientId(clientId)
            .filter { it.downloadedAt != null || !it.expiresAt.isAfter(now) || !clientArchiveExportStorage.exists(it.filePath) }
            .forEach { cleanupExport(it) }
    }

    private fun cleanupExport(request: ClientArchiveExportRecord) {
        clientArchiveExportStorage.delete(request.filePath)
        clientArchiveExportRecordRepository.delete(request)
    }

    private fun toStatusResponse(
        record: ClientArchiveExportRecord,
        clientId: Long
    ): ClientArchiveExportStatusResponse {
        val now = LocalDateTime.now(clock)
        val downloadAvailable = record.expiresAt.isAfter(now) && clientArchiveExportStorage.exists(record.filePath)

        return ClientArchiveExportStatusResponse(
            ready = record.downloadedAt == null && record.expiresAt.isAfter(now),
            format = record.exportFormat,
            requestedAt = record.requestedAt,
            requestedByEmployeeId = record.requestedByEmployeeId,
            downloadLink = if (downloadAvailable) {
                ClientArchiveExportDownloadLinkResponse(
                    downloadLink = buildDownloadLink(clientId, record.downloadToken),
                    downloadLinkExpiresAt = record.expiresAt,
                    downloadedAt = record.downloadedAt
                )
            } else {
                null
            }
        )
    }

    private fun buildDownloadLink(clientId: Long, downloadToken: String): String {
        return "/clients/$clientId/archive/export/$downloadToken"
    }
}
