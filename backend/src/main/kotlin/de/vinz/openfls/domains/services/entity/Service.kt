package de.vinz.openfls.domains.services.entity

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.categories.entity.Category
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.institutions.entity.Institution
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "services")
class Service(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,

        @field:NotNull
        var start: LocalDateTime = LocalDateTime.now(),

        @field:NotNull
        var end: LocalDateTime = LocalDateTime.now(),

        var minutes: Int = 0,

        @Column(length = 64)
        var title: String = "",

        @Column(length = 1024)
        var content: String = "",

        var groupService: Boolean = false,

        var unfinished: Boolean = false,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "client_id")
        var client: Client? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "employee_id")
        var employee: Employee? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "institution_id")
        var institution: Institution? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "hour_type_id")
        var hourType: HourType? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "assistance_plan_id")
        var assistancePlan: AssistancePlan? = null,

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
                name = "service_goals",
                joinColumns = [JoinColumn(name = "service_id")],
                inverseJoinColumns = [JoinColumn(name = "goal_id")])
        var goals: MutableSet<Goal> = mutableSetOf(),

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
                name = "service_categories",
                joinColumns = [JoinColumn(name = "service_id")],
                inverseJoinColumns = [JoinColumn(name = "category_id")])
        var categorys: MutableSet<Category> = mutableSetOf()
) {
        val archivedService: Boolean
                get() = client?.archived == true

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Service) return false
                return id == other.id
        }

        override fun hashCode(): Int {
                return id.hashCode()
        }

        override fun toString(): String {
                return "Service(" +
                        "id=$id," +
                        "start=$start," +
                        "end=$end," +
                        "minutes=$minutes," +
                        "title='$title'," +
                        "content='$content'," +
                        "group=$groupService," +
                        "unfinished=$unfinished"
        }
}
