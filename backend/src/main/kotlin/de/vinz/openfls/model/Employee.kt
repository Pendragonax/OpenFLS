package de.vinz.openfls.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.aspectj.weaver.tools.UnsupportedPointcutPrimitiveException
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.lang.Nullable
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "employees")
class Employee(
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    var id: Long? = null,

    //@field:NotEmpty(message = "Bitte geben sie einen Vornamen an")
    @Column(length = 64)
    var firstname: String,

    //@field:NotEmpty(message = "Bitte geben sie einen Nachnamen an")
    @Column(length = 64)
    var lastname: String,

    @Column(length = 32)
    var phonenumber: String,

    @field:Email
    @Column(length = 64)
    var email: String,

    var inactive: Boolean,

    @Column(length = 1024)
    var description: String,

    @OneToOne(
        mappedBy = "employee",
        cascade = [CascadeType.MERGE],
        fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    var access: EmployeeAccess?,

    @field:Nullable
    @OneToMany(
        mappedBy = "employee",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY)
    var permissions: MutableSet<Permission>?,

    @field:Nullable
    @JsonIgnoreProperties(value = ["sponsor", "hibernateLazyInitializer"])
    @OneToMany(
        mappedBy = "employee",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY)
    var unprofessionals: MutableSet<Unprofessional>?,

    @field:Nullable
    @OneToMany(
        mappedBy = "employee",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY)
    var contingents: MutableSet<Contingent>?,

    @JsonIgnore
    @OneToMany(
        mappedBy = "employee",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY)
    var services: MutableSet<Service> = mutableSetOf()
)