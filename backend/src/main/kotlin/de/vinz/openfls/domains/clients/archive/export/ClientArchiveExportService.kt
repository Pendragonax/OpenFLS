package de.vinz.openfls.domains.clients.archive.export

import com.fasterxml.jackson.databind.ObjectMapper
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.clients.archive.ClientArchiveActor
import de.vinz.openfls.domains.clients.archive.ClientArchiveService
import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportDto
import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportDownloadDto
import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportDownloadLinkDto
import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportStatusDto
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.exceptions.UserNotAllowedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Value
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

@Service
class ClientArchiveExportService(
    private val clientService: ClientService,
    private val clientArchiveService: ClientArchiveService,
    private val serviceRepository: ServiceRepository,
    private val assistancePlanRepository: AssistancePlanRepository,
    private val clientArchiveExportRequestRepository: ClientArchiveExportRequestRepository,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
    @param:Value("\${openfls.client-archive-export.download-link-ttl:20m}")
    private val exportDownloadLinkTtl: Duration
) {

    private val exportDirectory: String = Paths.get(System.getProperty("java.io.tmpdir"), "openfls-client-archive-exports").toString()

    @Transactional
    @Throws(UserNotAllowedException::class, ClientArchiveExportStateException::class)
    fun requestExport(
        clientId: Long,
        format: ClientArchiveExportFormat,
        anonymize: Boolean = false,
        actor: ClientArchiveActor
    ): ClientArchiveExportStatusDto {
        if (format != ClientArchiveExportFormat.JSON) {
            throw ClientArchiveExportStateException("unsupported export format")
        }

        val client = requireVisibleClient(clientId, actor)
        val now = LocalDateTime.now(clock)

        val exportData = ClientArchiveExportDto.from(
            client = client,
            services = serviceRepository.findByClientIdOrderByStartAsc(clientId),
            assistancePlans = assistancePlanRepository.findByClientId(clientId),
            anonymize = anonymize
        )
        val downloadToken = UUID.randomUUID().toString()
        val fileName = "client-$clientId-archive-export-$downloadToken.json"
        val exportFile = resolveExportFilePath(clientId, fileName)

        try {
            Files.createDirectories(exportFile.parent)
            Files.write(exportFile, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(exportData))

            val request = clientArchiveExportRequestRepository.save(
                ClientArchiveExportRequest(
                    downloadToken = downloadToken,
                    exportFormat = format,
                    requestedAt = now,
                    expiresAt = now.plus(exportDownloadLinkTtl),
                    fileName = fileName,
                    filePath = exportFile.toString(),
                    requestedByEmployeeId = actor.employeeId,
                    requestedByEmployeeFirstname = actor.firstname,
                    requestedByEmployeeLastname = actor.lastname,
                    client = client
                )
            )

            clientArchiveService.recordExport(
                clientId = clientId,
                actionDate = now.toLocalDate(),
                actionTimestamp = now,
                reason = "Export requested",
                remark = if (anonymize) "${format.name} [anonym]" else format.name,
                actor = actor,
                exportFormat = format
            )

            return toStatusDto(request, clientId)
        } catch (ex: Exception) {
            Files.deleteIfExists(exportFile)
            throw ex
        }
    }

    @Transactional(readOnly = true)
    @Throws(UserNotAllowedException::class)
    fun getExportStatus(
        clientId: Long,
        actor: ClientArchiveActor
    ): ClientArchiveExportStatusDto {
        requireVisibleClient(clientId, actor)
        val request = getActiveExport(clientId) ?: return emptyStatus()
        return toStatusDto(request, clientId)
    }

    @Transactional
    @Throws(ClientArchiveExportStateException::class)
    fun downloadExport(
        clientId: Long,
        downloadToken: String
    ): ClientArchiveExportDownloadDto {
        val request = clientArchiveExportRequestRepository.findByClientIdAndDownloadToken(clientId, downloadToken)
            ?: throw ClientArchiveExportStateException("export not found")

        val now = LocalDateTime.now(clock)
        if (request.downloadedAt != null || !request.expiresAt.isAfter(now)) {
            cleanupExport(request)
            throw ClientArchiveExportStateException("export unavailable")
        }

        val file = Path.of(request.filePath)
        if (!Files.exists(file)) {
            cleanupExport(request)
            throw ClientArchiveExportStateException("export file missing")
        }

        val content = Files.readAllBytes(file)
        Files.deleteIfExists(file)
        clientArchiveExportRequestRepository.delete(request)

        return ClientArchiveExportDownloadDto(
            fileName = request.fileName,
            content = content
        )
    }

    private fun requireVisibleClient(
        clientId: Long,
        actor: ClientArchiveActor
    ) = clientService.getById(clientId)?.also { client ->
        if (!actor.isAdmin && !actor.leadingInstitutionIds.contains(client.institution?.id ?: 0)) {
            throw UserNotAllowedException()
        }
    } ?: throw IllegalArgumentException("client not found")

    private fun getActiveExport(clientId: Long): ClientArchiveExportRequest? {
        val now = LocalDateTime.now(clock)
        return clientArchiveExportRequestRepository.findAllByClientId(clientId)
            .sortedByDescending { it.requestedAt }
            .firstOrNull { it.downloadedAt == null && it.expiresAt.isAfter(now) && Files.exists(Path.of(it.filePath)) }
            ?: run {
                cleanupExpiredExports(clientId, now)
                null
            }
    }

    private fun cleanupExpiredExports(clientId: Long, now: LocalDateTime) {
        clientArchiveExportRequestRepository.findAllByClientId(clientId)
            .filter { it.downloadedAt != null || !it.expiresAt.isAfter(now) || !Files.exists(Path.of(it.filePath)) }
            .forEach { cleanupExport(it) }
    }

    private fun cleanupExport(request: ClientArchiveExportRequest) {
        Files.deleteIfExists(Path.of(request.filePath))
        clientArchiveExportRequestRepository.delete(request)
    }

    private fun toStatusDto(
        request: ClientArchiveExportRequest,
        clientId: Long
    ): ClientArchiveExportStatusDto {
        return ClientArchiveExportStatusDto().apply {
            ready = request.downloadedAt == null && request.expiresAt.isAfter(LocalDateTime.now(clock))
            format = request.exportFormat
            requestedAt = request.requestedAt
            requestedByEmployeeId = request.requestedByEmployeeId
            downloadLink = request.downloadedAt?.let {
                null
            } ?: if (request.expiresAt.isAfter(LocalDateTime.now(clock)) && Files.exists(Path.of(request.filePath))) {
                ClientArchiveExportDownloadLinkDto().apply {
                    downloadLink = buildDownloadLink(clientId, request.downloadToken)
                    downloadLinkExpiresAt = request.expiresAt
                    downloadedAt = request.downloadedAt
                }
            } else {
                null
            }
        }
    }

    private fun emptyStatus(): ClientArchiveExportStatusDto {
        return ClientArchiveExportStatusDto().apply {
            ready = false
            downloadLink = null
        }
    }

    private fun buildDownloadLink(clientId: Long, downloadToken: String): String {
        return "/clients/$clientId/archive/export/$downloadToken"
    }

    private fun resolveExportFilePath(clientId: Long, fileName: String): Path {
        return Paths.get(exportDirectory, clientId.toString(), fileName)
    }
}
