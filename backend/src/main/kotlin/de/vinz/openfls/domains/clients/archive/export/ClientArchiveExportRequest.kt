package de.vinz.openfls.domains.clients.archive.export

import de.vinz.openfls.domains.clients.Client
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "client_archive_export_requests")
class ClientArchiveExportRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "download_token", length = 64, nullable = false, unique = true)
    var downloadToken: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "export_format", length = 32, nullable = false)
    var exportFormat: ClientArchiveExportFormat = ClientArchiveExportFormat.JSON,

    @Column(name = "requested_at", nullable = false)
    var requestedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "downloaded_at")
    var downloadedAt: LocalDateTime? = null,

    @Column(name = "file_name", length = 255, nullable = false)
    var fileName: String = "",

    @Column(name = "file_path", length = 1024, nullable = false)
    var filePath: String = "",

    @Column(name = "requested_by_employee_id", nullable = false)
    var requestedByEmployeeId: Long = 0,

    @Column(name = "requested_by_employee_firstname", length = 64, nullable = false)
    var requestedByEmployeeFirstname: String = "",

    @Column(name = "requested_by_employee_lastname", length = 64, nullable = false)
    var requestedByEmployeeLastname: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    var client: Client? = null
)
