package de.vinz.openfls.domains.clientTasks.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Audit trail of every change on a [ClientTask]. Entries are written by
 * [ClientTaskService] and are never updated or removed afterwards.
 */
@Entity
@Table(name = "client_task_audit_logs")
class ClientTaskAuditLog(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,

        @Column(name = "client_task_id", nullable = false)
        var clientTaskId: Long = 0,

        @Column(name = "client_id", nullable = false)
        var clientId: Long = 0,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 16)
        var action: ClientTaskAuditAction = ClientTaskAuditAction.CREATE,

        @Column(name = "changed_at", nullable = false)
        var changedAt: LocalDateTime = LocalDateTime.now(),

        @Column(name = "actor_employee_id")
        var actorEmployeeId: Long? = null,

        @Column(nullable = false, length = 128)
        var actor: String = "system",

        @Column(name = "before_title", length = 128)
        var beforeTitle: String? = null,

        @Column(name = "after_title", length = 128)
        var afterTitle: String? = null,

        @Column(name = "before_description", length = 1024)
        var beforeDescription: String? = null,

        @Column(name = "after_description", length = 1024)
        var afterDescription: String? = null,

        @Column(name = "before_due_date")
        var beforeDueDate: LocalDate? = null,

        @Column(name = "after_due_date")
        var afterDueDate: LocalDate? = null,

        @Column(name = "before_done")
        var beforeDone: Boolean? = null,

        @Column(name = "after_done")
        var afterDone: Boolean? = null,

        @Column(name = "comment", length = 1024)
        var comment: String? = null
)
