package de.vinz.openfls.domains.clients.archive.export

import org.springframework.data.repository.CrudRepository

interface ClientArchiveExportRequestRepository : CrudRepository<ClientArchiveExportRequest, Long> {
    fun findByClientIdAndDownloadToken(clientId: Long, downloadToken: String): ClientArchiveExportRequest?

    fun findAllByClientId(clientId: Long): List<ClientArchiveExportRequest>
}
