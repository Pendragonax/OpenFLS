package de.vinz.openfls.domains.clients.repository

import de.vinz.openfls.domains.clients.entity.ClientArchiveExportRecord
import org.springframework.data.repository.CrudRepository

interface ClientArchiveExportRecordRepository : CrudRepository<ClientArchiveExportRecord, Long> {
    fun findByClientIdAndDownloadToken(clientId: Long, downloadToken: String): ClientArchiveExportRecord?

    fun findAllByClientId(clientId: Long): List<ClientArchiveExportRecord>
}
