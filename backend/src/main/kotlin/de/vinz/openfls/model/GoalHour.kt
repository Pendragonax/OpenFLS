package de.vinz.openfls.model

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "goal_hours")
class GoalHour (
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    var id: Long = 0,

    @field:NotNull(message = "weekly hours are null")
    @Column(precision = 7, scale = 2)
    var weeklyHours: Double,

    @JsonIgnore
    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "hour_type_id")
    var hourType: HourType,

    @JsonIgnore
    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "goal_id")
    var goal: Goal
)