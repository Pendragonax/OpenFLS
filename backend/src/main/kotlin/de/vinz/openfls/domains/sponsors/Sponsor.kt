package de.vinz.openfls.domains.sponsors

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.categories.entities.Category
import de.vinz.openfls.domains.employees.entities.Unprofessional
import org.springframework.lang.Nullable
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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Sponsor) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        fun from(sponsorDto: SponsorDto): Sponsor {
            return Sponsor(
                    id = sponsorDto.id,
                    name = sponsorDto.name,
                    payExact = sponsorDto.payExact,
                    payOverhang = sponsorDto.payOverhang,
                    unprofessionals = sponsorDto.unprofessionals?.map { Unprofessional.from(it) }?.toMutableSet()
            )
        }
    }
}