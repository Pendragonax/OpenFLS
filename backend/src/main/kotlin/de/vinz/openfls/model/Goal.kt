package de.vinz.openfls.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.lang.Nullable
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
@Table(name = "goals")
class Goal(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long,

    @field:NotEmpty(message = "title needed")
    @Column(length = 124)
    var title: String,

    @Column(length = 1024)
    var description: String,

    @JsonIgnore
    @field:Nullable
    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "institution_id")
    var institution: Institution?,

    @JsonIgnore
    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "assistance_plan_id")
    var assistancePlan: AssistancePlan,

    @OneToMany(
        mappedBy = "goal",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY)
    var hours: MutableSet<GoalHour> = mutableSetOf(),

    @ManyToMany(mappedBy = "goals")
    var services: MutableSet<Service> = mutableSetOf()
)