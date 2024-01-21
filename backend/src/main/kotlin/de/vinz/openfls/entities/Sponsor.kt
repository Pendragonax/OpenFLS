package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.lang.Nullable
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
@Table(name = "sponsors")
class Sponsor(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long,

    @field:NotEmpty
    @Column(length = 32)
    var name: String,

    @field:NotNull
    var payOverhang: Boolean,

    @field:NotNull
    var payExact: Boolean,

    @field:Nullable
    @JsonIgnoreProperties(value = ["employee", "hibernateLazyInitializer"])
    @OneToMany(
        mappedBy = "sponsor",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY)
    var unprofessionals: MutableSet<Unprofessional>?,

    @JsonIgnore
    @OneToMany(
        mappedBy = "sponsor",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY
    )
    var assistancePlans: MutableSet<AssistancePlan>?
)