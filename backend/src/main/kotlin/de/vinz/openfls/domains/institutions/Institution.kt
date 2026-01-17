package de.vinz.openfls.domains.institutions

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.categories.entities.Category
import de.vinz.openfls.domains.contingents.Contingent
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.permissions.Permission
import de.vinz.openfls.domains.services.Service
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty

@Entity
@Table(name = "institutions")
class Institution(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @field:NotEmpty(message = "Bitte geben sie einen Vornamen an")
        @Column(length = 64)
        var name: String = "",

        @Column(length = 32)
        var phonenumber: String = "",

        @field:Email
        @Column(length = 64)
        var email: String = "",

        @JsonIgnore
        @OneToMany(
                mappedBy = "institution",
                cascade = [CascadeType.REMOVE],
                fetch = FetchType.LAZY)
        var permissions: MutableSet<Permission>? = null,

        @OneToMany(
                mappedBy = "institution",
                cascade = [CascadeType.REMOVE],
                fetch = FetchType.LAZY)
        var contingents: MutableSet<Contingent>? = null,

        @OneToMany(
                mappedBy = "institution",
                cascade = [CascadeType.REMOVE],
                fetch = FetchType.LAZY
        )
        var assistancePlans: MutableSet<AssistancePlan>? = null,

        @OneToMany(
                mappedBy = "institution",
                cascade = [CascadeType.REMOVE],
                fetch = FetchType.LAZY
        )
        var goals: MutableSet<Goal> = mutableSetOf(),

        @JsonIgnore
        @OneToMany(
                mappedBy = "institution",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        var services: MutableSet<Service> = mutableSetOf()
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Institution) return false
                return id == other.id
        }

        override fun hashCode(): Int {
                return id?.hashCode() ?: 0
        }
}
