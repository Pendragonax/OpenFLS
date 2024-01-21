package de.vinz.openfls.entities

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

@Entity
@Table(name = "employees_access")
class EmployeeAccess(
    @Id
    var id: Long? = null,

    @field:NotEmpty
    @field:Size(min = 6)
    @Column(length = 32)
    var username: String,

    @field:NotEmpty
    @field:Size(min = 6)
    var password: String,

    var role: Int,

    @OneToOne(cascade = [CascadeType.ALL])
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapsId
    @JoinColumn(name = "id", referencedColumnName = "id")
    var employee: Employee?
    )