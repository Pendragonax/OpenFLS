package de.vinz.openfls.domains.assistancePlans

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.hourTypes.HourType
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "assistance_plan_hours")
class AssistancePlanHour(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,

        @field:NotNull(message = "weekly minutes are null")
        @Column(name = "weekly_minutes")
        var weeklyMinutes: Int = 0,

        @JsonIgnoreProperties(value = ["assistancePlanHours", "hibernateLazyInitializer"])
        @ManyToOne(
                cascade = [CascadeType.PERSIST],
                fetch = FetchType.LAZY
        )
        @JoinColumn(name = "hour_type_id")
        var hourType: HourType? = null,

        @ManyToOne(
                cascade = [CascadeType.PERSIST],
                fetch = FetchType.LAZY
        )
        @JoinColumn(name = "assistance_plan_id")
        var assistancePlan: AssistancePlan? = null
)
