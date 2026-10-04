package de.vinz.openfls.domains.employees.entity

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.contingents.entity.Contingent
import de.vinz.openfls.domains.evaluations.entity.Evaluation
import de.vinz.openfls.domains.permissions.entity.Permission
import de.vinz.openfls.domains.services.entity.Service
import jakarta.persistence.*
import jakarta.validation.constraints.Email

@Entity
@Table(name = "employees")
class Employee(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        //@field:NotEmpty(message = "Bitte geben sie einen Vornamen an")
        @Column(length = 64)
        var firstname: String = "",

        //@field:NotEmpty(message = "Bitte geben sie einen Nachnamen an")
        @Column(length = 64)
        var lastname: String = "",

        @Column(length = 32)
        var phonenumber: String = "",

        @field:Email
        @Column(length = 64)
        var email: String = "",

        @Column(nullable = false)
        var archived: Boolean = false,

        var inactive: Boolean = false,

        @Column(length = 1024)
        var description: String = "",

        @OneToOne(
                mappedBy = "employee",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        @PrimaryKeyJoinColumn
        var access: EmployeeAccess? = null,

        @OneToMany(
                mappedBy = "employee",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        var permissions: MutableSet<Permission>? = null,

        @OneToMany(
                mappedBy = "employee",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        var unprofessionals: MutableSet<Unprofessional>? = null,

        @OneToMany(
                mappedBy = "employee",
                cascade = [CascadeType.REMOVE],
                fetch = FetchType.LAZY)
        var contingents: MutableSet<Contingent>? = null,

        @OneToMany(
                mappedBy = "createdBy",
                cascade = [CascadeType.REFRESH],
                fetch = FetchType.LAZY)
        var createdEvaluations: MutableSet<Evaluation> = mutableSetOf(),

        @OneToMany(
                mappedBy = "updatedBy",
                cascade = [CascadeType.REFRESH],
                fetch = FetchType.LAZY)
        var updatedEvaluations: MutableSet<Evaluation> = mutableSetOf(),

        @OneToMany(
                mappedBy = "employee",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        var services: MutableSet<Service> = mutableSetOf(),

        @OneToMany(
                mappedBy = "employee",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY,
                orphanRemoval = true)
        @OrderBy("actionTimestamp DESC")
        var archiveHistoryEntries: MutableList<EmployeeArchiveHistoryEntry> = mutableListOf(),

        @ManyToMany(
                fetch = FetchType.LAZY
        )
        @JoinTable(
                name = "assistance_plan_favorites",
                joinColumns = [JoinColumn(name = "employee_id")],
                inverseJoinColumns = [JoinColumn(name = "assistance_plan_id")])
        var assistancePlanFavorites: MutableSet<AssistancePlan> = mutableSetOf(),

        @ManyToMany(
                fetch = FetchType.LAZY
        )
        @JoinTable(
                name = "client_favorites",
                joinColumns = [JoinColumn(name = "employee_id")],
                inverseJoinColumns = [JoinColumn(name = "client_id")])
        var clientFavorites: MutableSet<Client> = mutableSetOf(),
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Employee) return false
                return id == other.id
        }

        override fun hashCode(): Int {
                return id?.hashCode() ?: 0
        }
}
