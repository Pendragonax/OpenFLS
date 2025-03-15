package de.vinz.openfls.domains.hourTypes

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.services.Service
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import org.jetbrains.annotations.NotNull

@Entity
@Table(name = "hour_types")
class HourType(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,

        @field:NotEmpty(message = "Title is required.")
        @Column(length = 64)
        var title: String = "",

        @field:NotNull
        var price: Double = 0.0,

        @JsonIgnore
        @OneToMany(mappedBy = "hourType", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
        var services: MutableSet<Service> = mutableSetOf(),

        @JsonIgnore
        @OneToMany(mappedBy = "hourType", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
        var assistancePlanHours: MutableSet<AssistancePlanHour> = mutableSetOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HourType) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode() ?: 0
    }

    companion object {
        fun from(hourTypeDto: HourTypeDto): HourType {
            return HourType(
                    id = hourTypeDto.id,
                    title = hourTypeDto.title,
                    price = hourTypeDto.price)
        }
    }
}