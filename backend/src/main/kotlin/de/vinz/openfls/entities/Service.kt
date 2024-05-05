package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "services")
class Service(
        @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,

        @field:NotNull()
    var start: LocalDateTime = LocalDateTime.now(),

        @field:NotNull()
    var end: LocalDateTime = LocalDateTime.now(),

        var minutes: Int = 0,

        @Column(length = 64)
    var title: String = "",

        @Column(length = 1024)
    var content: String = "",

        var unfinished: Boolean = false,

        @JsonIgnoreProperties(value = ["services", "categoryTemplate", "assistancePlans", "institution", "hibernateLazyInitializer"])
    @ManyToOne(
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "client_id")
    var client: Client,

        @JsonIgnoreProperties(value = ["services", "permissions", "contingents", "unprofessionals", "access", "hibernateLazyInitializer"])
    @ManyToOne(
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "employee_id")
    var employee: Employee,

        @JsonIgnoreProperties(value = ["services", "assistancePlans", "contingents", "goals", "hibernateLazyInitializer"])
    @ManyToOne(
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "institution_id")
    var institution: Institution,

        @JsonIgnoreProperties(value = ["services", "hibernateLazyInitializer"])
    @ManyToOne(
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "hour_type_id")
    var hourType: HourType,

        @JsonIgnoreProperties(value = ["services", "hours", "goals", "hibernateLazyInitializer"])
    @ManyToOne(
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "assistance_plan_id")
    var assistancePlan: AssistancePlan,

        @ManyToMany(
        fetch = FetchType.LAZY
    )
    @JoinTable(
        name = "service_goals",
        joinColumns = [JoinColumn(name = "service_id")],
        inverseJoinColumns = [JoinColumn(name = "goal_id")])
    @JsonIgnoreProperties(value = ["services", "hours", "hibernateLazyInitializer"])
    var goals: MutableSet<Goal> = mutableSetOf(),

        @JsonIgnore
    @ManyToMany(
        fetch = FetchType.LAZY
    )
    @JoinTable(
        name = "service_categories",
        joinColumns = [JoinColumn(name = "service_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")])
    @JsonIgnoreProperties(value = ["services", "categoryTemplate", "hibernateLazyInitializer"])
    var categorys: MutableSet<Category> = mutableSetOf()
)