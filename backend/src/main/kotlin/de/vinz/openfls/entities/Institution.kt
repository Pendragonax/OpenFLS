package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.contingents.Contingent
import org.springframework.lang.Nullable
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "institutions")
class Institution(
        @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    var id: Long? = null,

        @field:NotEmpty(message = "Bitte geben sie einen Vornamen an")
    @Column(length = 64)
    var name: String,

        @Column(length = 32)
    var phonenumber: String,

        @field:Email
    @Column(length = 64)
    var email: String,

        @JsonIgnore
    @field:Nullable
    @OneToMany(
        mappedBy = "institution",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY)
    var permissions: MutableSet<Permission>?,

        @field:Nullable
    @OneToMany(
        mappedBy = "institution",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY)
    var contingents: MutableSet<Contingent>?,

        @OneToMany(
        mappedBy = "institution",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY
    )
    var assistancePlans: MutableSet<AssistancePlan>?,

        @OneToMany(
        mappedBy = "institution",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY
    )
    var goals: MutableSet<Goal>,

        @JsonIgnore
    @OneToMany(
        mappedBy = "institution",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY)
    var services: MutableSet<Service> = mutableSetOf()
)