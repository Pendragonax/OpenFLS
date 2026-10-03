package de.vinz.openfls.domains.clients.entity

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.categories.entity.CategoryTemplate
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.services.entity.Service
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
class Client(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,

        @field:NotBlank
        @Column(length = 64)
        var firstName: String = "",

        @field:NotBlank
        @Column(length = 64)
        var lastName: String = "",

        @Column(length = 32)
        var phoneNumber: String = "",

        @Column(length = 64)
        var email: String = "",

        @Column(nullable = false)
        var archived: Boolean = false,

        @ManyToOne(
                cascade = [CascadeType.PERSIST],
                fetch = FetchType.LAZY
        )
        @JoinColumn(name = "category_template_id")
        var categoryTemplate: CategoryTemplate? = null,

        @OneToMany(
                cascade = [CascadeType.ALL],
                mappedBy = "client",
                fetch = FetchType.LAZY
        )
        var assistancePlans: MutableSet<AssistancePlan> = mutableSetOf(),

        @ManyToOne(
                cascade = [CascadeType.PERSIST],
                fetch = FetchType.LAZY
        )
        @JoinColumn(name = "institution_id")
        var institution: Institution? = null,

        @OneToMany(
                mappedBy = "client",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        var services: MutableSet<Service> = mutableSetOf(),

        @OneToMany(
                mappedBy = "client",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY,
                orphanRemoval = true)
        @OrderBy("actionTimestamp DESC")
        var archiveHistoryEntries: MutableList<ClientArchiveHistoryEntry> = mutableListOf()
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Client) return false
                return id == other.id
        }

        override fun hashCode(): Int {
                return id.hashCode()
        }
}
