package de.vinz.openfls.domains.clientTasks.entity

import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.employees.entities.Employee
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A task that belongs to a client. Every employee may read, create and complete
 * tasks; who changed what and when is kept in [ClientTaskAuditLog].
 */
@Entity
@Table(name = "client_tasks")
class ClientTask(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "client_id", nullable = false)
        var client: Client? = null,

        @Column(nullable = false, length = 128)
        var title: String = "",

        @Column(length = 1024)
        var description: String = "",

        @Column(name = "due_date", nullable = false)
        var dueDate: LocalDate = LocalDate.now(),

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "created_by_id")
        var createdBy: Employee? = null,

        @Column(name = "created_at", nullable = false)
        var createdAt: LocalDateTime = LocalDateTime.now(),

        @Column(nullable = false)
        var done: Boolean = false,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "completed_by_id")
        var completedBy: Employee? = null,

        @Column(name = "completed_at")
        var completedAt: LocalDateTime? = null,

        @Column(name = "completed_on")
        var completedOn: LocalDate? = null,

        @Column(name = "completion_comment", length = 1024)
        var completionComment: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientTask) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
