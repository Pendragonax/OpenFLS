package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.contingents.Contingent
import de.vinz.openfls.domains.evaluations.Evaluation
import org.springframework.lang.Nullable
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

        var inactive: Boolean = false,

        @Column(length = 1024)
        var description: String = "",

        @OneToOne(
                mappedBy = "employee",
                cascade = [CascadeType.MERGE],
                fetch = FetchType.LAZY)
        @PrimaryKeyJoinColumn
        @JsonIgnore
        var access: EmployeeAccess? = null,

        @field:Nullable
        @OneToMany(
                mappedBy = "employee",
                cascade = [CascadeType.REMOVE],
                fetch = FetchType.LAZY)
        var permissions: MutableSet<Permission>? = null,

        @field:Nullable
        @JsonIgnoreProperties(value = ["sponsor", "hibernateLazyInitializer"])
        @OneToMany(
                mappedBy = "employee",
                cascade = [CascadeType.REMOVE],
                fetch = FetchType.LAZY)
        var unprofessionals: MutableSet<Unprofessional>? = null,

        @field:Nullable
        @OneToMany(
                mappedBy = "employee",
                cascade = [CascadeType.REMOVE],
                fetch = FetchType.LAZY)
        var contingents: MutableSet<Contingent>? = null,

        @JsonIgnore
        @OneToMany(
                mappedBy = "createdBy",
                cascade = [CascadeType.REFRESH],
                fetch = FetchType.LAZY)
        var createdEvaluations: MutableSet<Evaluation> = mutableSetOf(),

        @JsonIgnore
        @OneToMany(
                mappedBy = "updatedBy",
                cascade = [CascadeType.REFRESH],
                fetch = FetchType.LAZY)
        var updatedEvaluations: MutableSet<Evaluation> = mutableSetOf(),

        @JsonIgnore
        @OneToMany(
                mappedBy = "employee",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        var services: MutableSet<Service> = mutableSetOf(),

        @ManyToMany(
                fetch = FetchType.LAZY
        )
        @JoinTable(
                name = "assistance_plan_favorites",
                joinColumns = [JoinColumn(name = "employee_id")],
                inverseJoinColumns = [JoinColumn(name = "assistance_plan_id")])
        @JsonIgnoreProperties(value = ["employees", "hibernateLazyInitializer"])
        var assistancePlanFavorites: MutableSet<AssistancePlan> = mutableSetOf(),
)