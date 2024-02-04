package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import org.jetbrains.annotations.NotNull
import javax.persistence.*

@Entity
@Table(name = "hour_types")
class HourType(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long,

    @field:NotNull
    @Column(length = 64)
    var title: String,

    @field:NotNull
    @Column(precision = 5, scale = 2)
    var price: Double,

    @JsonIgnore
    @OneToMany(
        mappedBy = "hourType",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY)
    var services: MutableSet<Service> = mutableSetOf(),

    @JsonIgnore
    @OneToMany(
            mappedBy = "hourType",
            cascade = [CascadeType.ALL],
            fetch = FetchType.LAZY)
    var assistancePlanHours: MutableSet<AssistancePlanHour> = mutableSetOf()
)