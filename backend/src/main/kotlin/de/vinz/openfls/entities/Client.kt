package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
class Client(
        @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long,

        @field:NotBlank
    @Column(length = 64)
    var firstName: String,

        @field:NotBlank
    @Column(length = 64)
    var lastName: String,

        @Column(length = 32)
    var phoneNumber: String,

        @Column(length = 64)
    var email: String,

        @JsonIgnoreProperties(value = ["hibernateLazyInitializer"])
    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "category_template_id")
    var categoryTemplate: CategoryTemplate,

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
    var institution: Institution,

        @JsonIgnore
    @OneToMany(
        mappedBy = "client",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY)
    var services: MutableSet<Service>
)
