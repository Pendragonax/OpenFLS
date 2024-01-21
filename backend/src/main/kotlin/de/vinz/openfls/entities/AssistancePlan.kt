package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "assistance_plans")
class AssistancePlan(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long,

    //@field:NotNull(message = "start is null")
    var start: LocalDate = LocalDate.now(),

    //@field:NotNull(message = "end is null")
    var end: LocalDate = LocalDate.now(),

    @JsonIgnore
    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "client_id")
    var client: Client,

    @JsonIgnore
    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "sponsor_id")
    var sponsor: Sponsor,

    @JsonIgnore
    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "institution_id")
    var institution: Institution,

    @OneToMany(
        cascade = [CascadeType.ALL],
        mappedBy = "assistancePlan",
        fetch = FetchType.LAZY
    )
    var goals: MutableSet<Goal> = mutableSetOf(),

    @OneToMany(
        cascade = [CascadeType.ALL],
        mappedBy = "assistancePlan",
        fetch = FetchType.LAZY
    )
    var hours: MutableSet<AssistancePlanHour> = mutableSetOf(),

    @OneToMany(
        mappedBy = "assistancePlan",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY)
    var services: MutableSet<Service> = mutableSetOf(),

    @ManyToMany(mappedBy = "assistancePlanFavorites")
    var employees: MutableSet<Employee> = mutableSetOf()
)