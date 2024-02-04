package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "assistance_plan_hours")
class AssistancePlanHour (
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    var id: Long = 0,

    @field:NotNull(message = "weekly hours are null")
    @Column(precision = 7, scale = 2)
    var weeklyHours: Double,

    @JsonIgnoreProperties(value = ["assistancePlanHours", "hibernateLazyInitializer"])
    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "hour_type_id")
    var hourType: HourType,

    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "assistance_plan_id")
    var assistancePlan: AssistancePlan
)