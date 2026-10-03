package de.vinz.openfls.domains.hourCorridors.entity

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.hourTypes.entity.HourType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "hour_corridors")
class HourCorridor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(length = 64)
    var title: String = "",

    @Column(name = "weekly_minutes_from")
    var weeklyMinutesFrom: Int = 0,

    @Column(name = "weekly_minutes_till")
    var weeklyMinutesTill: Int = 0,

    @field:NotNull(message = "Hour type is required.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hour_type_id")
    var hourType: HourType? = null,

    @OneToMany(mappedBy = "hourCorridor", fetch = FetchType.LAZY)
    var assistancePlans: MutableSet<AssistancePlan> = mutableSetOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HourCorridor) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
