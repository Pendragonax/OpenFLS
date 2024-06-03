package de.vinz.openfls.domains.client

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.entities.CategoryTemplate
import de.vinz.openfls.entities.Institution
import de.vinz.openfls.entities.Service
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
class Client(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
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

        @JsonIgnoreProperties(value = ["hibernateLazyInitializer"])
        @ManyToOne(
                cascade = [CascadeType.PERSIST],
                fetch = FetchType.LAZY
        )
        @JoinColumn(name = "category_template_id")
        var categoryTemplate: CategoryTemplate? = null,

        @JsonIgnoreProperties(value = ["client", "services", "hibernateLazyInitializer"])
        @OneToMany(
                cascade = [CascadeType.ALL],
                mappedBy = "client",
                fetch = FetchType.LAZY
        )
        var assistancePlans: MutableSet<AssistancePlan> = mutableSetOf(),

        @JsonIgnoreProperties(value = ["contingents", "assistancePlans", "goals", "hibernateLazyInitializer"])
        @ManyToOne(
                cascade = [CascadeType.PERSIST],
                fetch = FetchType.LAZY
        )
        @JoinColumn(name = "institution_id")
        var institution: Institution? = null,

        @JsonIgnore
        @OneToMany(
                mappedBy = "client",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        var services: MutableSet<Service> = mutableSetOf()
)
