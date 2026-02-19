package de.vinz.openfls.domains.goals.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.categories.entities.Category
import de.vinz.openfls.domains.hourTypes.HourType
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "goal_hours")
class GoalHour(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,

        @field:NotNull(message = "Weekly minutes are required")
        @Column(name = "weekly_minutes")
        var weeklyMinutes: Int = 0,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @JoinColumn(name = "hour_type_id")
        var hourType: HourType? = null,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @JoinColumn(name = "goal_id")
        var goal: Goal? = null
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is GoalHour) return false
                return id == other.id
        }

        override fun hashCode(): Int {
                return id.hashCode()
        }
}
