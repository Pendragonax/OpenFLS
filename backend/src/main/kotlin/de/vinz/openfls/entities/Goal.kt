package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.evaluations.entities.Evaluation
import org.springframework.lang.Nullable
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty

@Entity
@Table(name = "goals")
class Goal(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0,

        @field:NotEmpty(message = "title needed")
        @Column(length = 124)
        var title: String = "",

        @Column(length = 1024)
        var description: String = "",

        @JsonIgnore
        @field:Nullable
        @ManyToOne(
                cascade = [CascadeType.PERSIST],
                fetch = FetchType.LAZY
        )
        @JoinColumn(name = "institution_id")
        var institution: Institution? = null,

        @JsonIgnore
        @ManyToOne(
                cascade = [CascadeType.PERSIST],
                fetch = FetchType.LAZY
        )
        @JoinColumn(name = "assistance_plan_id")
        var assistancePlan: AssistancePlan? = null,

        @OneToMany(
                mappedBy = "goal",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        var hours: MutableSet<GoalHour> = mutableSetOf(),

        @OneToMany(
                mappedBy = "goal",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        var evaluations: MutableSet<Evaluation> = mutableSetOf(),

        @ManyToMany(mappedBy = "goals")
        var services: MutableSet<Service> = mutableSetOf()
)