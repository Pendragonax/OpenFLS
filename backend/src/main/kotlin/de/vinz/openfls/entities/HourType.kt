package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import org.jetbrains.annotations.NotNull

@Entity
@Table(name = "hour_types")
class HourType(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY) // Optimiert für SQL-Datenbanken
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
)