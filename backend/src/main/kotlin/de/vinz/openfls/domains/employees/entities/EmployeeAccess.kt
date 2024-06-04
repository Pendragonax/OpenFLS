package de.vinz.openfls.domains.employees.entities

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@Entity
@Table(name = "employees_access")
class EmployeeAccess(
        @Id
        var id: Long? = null,

        @field:NotEmpty(message = "Username is required.")
        @field:Size(min = 6, max = 32, message = "Username must be between 6 and 32 characters.")
        @Column(length = 32)
        var username: String = "",

        @field:NotEmpty(message = "Password is required.")
        @field:Size(min = 6, message = "Password must be at least 6 characters.")
        var password: String = "",

        var role: Int = 0,

        @OneToOne(cascade = [CascadeType.ALL])
        @OnDelete(action = OnDeleteAction.CASCADE)
        @MapsId
        @JoinColumn(name = "id", referencedColumnName = "id")
        var employee: Employee? = null
)