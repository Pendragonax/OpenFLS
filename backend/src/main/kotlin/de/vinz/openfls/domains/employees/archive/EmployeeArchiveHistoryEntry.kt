package de.vinz.openfls.domains.employees.archive

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.employees.entities.Employee
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "employee_archive_history_entries")
class EmployeeArchiveHistoryEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 32, nullable = false)
    var actionType: EmployeeArchiveActionType = EmployeeArchiveActionType.ARCHIVE,

    @field:NotNull
    @Column(name = "action_date", nullable = false)
    var actionDate: LocalDate = LocalDate.now(),

    @field:NotNull
    @Column(name = "action_timestamp", nullable = false)
    var actionTimestamp: LocalDateTime = LocalDateTime.now(),

    @Column(length = 1024)
    var reason: String = "",

    @Column(length = 1024)
    var remark: String = "",

    @field:NotNull
    @Column(name = "executing_employee_id", nullable = false)
    var executingEmployeeId: Long = 0,

    @field:NotNull
    @Column(name = "executing_employee_firstname", length = 64, nullable = false)
    var executingEmployeeFirstname: String = "",

    @field:NotNull
    @Column(name = "executing_employee_lastname", length = 64, nullable = false)
    var executingEmployeeLastname: String = "",

    @JsonIgnoreProperties(value = ["archiveHistoryEntries", "assistancePlanFavorites", "services", "hibernateLazyInitializer"])
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    var employee: Employee? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmployeeArchiveHistoryEntry) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
