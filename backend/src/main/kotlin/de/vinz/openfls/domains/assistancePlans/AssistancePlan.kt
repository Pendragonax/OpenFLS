package de.vinz.openfls.domains.assistancePlans

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.services.Service
import de.vinz.openfls.domains.sponsors.Sponsor
import java.time.LocalDate
import jakarta.persistence.*

@Entity
@Table(name = "assistance_plans")
class AssistancePlan(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,  // Defaultwert, wird von Hibernate überschrieben

        var start: LocalDate = LocalDate.now(),

        var end: LocalDate = LocalDate.now(),

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @JoinColumn(name = "client_id")
        var client: Client? = null,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @JoinColumn(name = "sponsor_id")
        var sponsor: Sponsor? = null,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @JoinColumn(name = "institution_id")
        var institution: Institution? = null,

        @OneToMany(cascade = [CascadeType.ALL], mappedBy = "assistancePlan", fetch = FetchType.LAZY)
        var goals: MutableSet<Goal> = mutableSetOf(),

        @OneToMany(cascade = [CascadeType.ALL], mappedBy = "assistancePlan", fetch = FetchType.LAZY)
        var hours: MutableSet<AssistancePlanHour> = mutableSetOf(),

        @OneToMany(mappedBy = "assistancePlan", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
        var services: MutableSet<Service> = mutableSetOf(),

        @ManyToMany(mappedBy = "assistancePlanFavorites")
        var employees: MutableSet<Employee> = mutableSetOf()
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is AssistancePlan) return false
                return id == other.id
        }

        override fun hashCode(): Int {
                return id.hashCode()
        }
}
