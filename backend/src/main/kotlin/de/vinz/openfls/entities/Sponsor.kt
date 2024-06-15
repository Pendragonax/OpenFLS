package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.employees.entities.Unprofessional
import org.springframework.lang.Nullable
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "sponsors")
class Sponsor(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0,

        @field:NotEmpty
        @Column(length = 32)
        var name: String = "",

        @field:NotNull
        var payOverhang: Boolean = false,

        @field:NotNull
        var payExact: Boolean = false,

        @field:Nullable
        @JsonIgnoreProperties(value = ["employee", "hibernateLazyInitializer"])
        @OneToMany(
                mappedBy = "sponsor",
                cascade = [CascadeType.REMOVE],
                fetch = FetchType.LAZY)
        var unprofessionals: MutableSet<Unprofessional>? = null,

        @JsonIgnore
        @OneToMany(
                mappedBy = "sponsor",
                cascade = [CascadeType.REMOVE],
                fetch = FetchType.LAZY
        )
        var assistancePlans: MutableSet<AssistancePlan>? = null
)