package de.vinz.openfls.domains.sponsors.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.categories.entity.Category
import de.vinz.openfls.domains.employees.entity.Unprofessional
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "sponsors")
class Sponsor(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,

        @field:NotEmpty
        @Column(length = 32)
        var name: String = "",

        @field:NotNull
        var payOverhang: Boolean = false,

        @field:NotNull
        var payExact: Boolean = false,

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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Sponsor) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
