package de.vinz.openfls.domains.permissions.entity

import java.io.Serializable
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class PermissionKey(
        @Column(name = "employee_Id") var employeeId: Long? = null,
        @Column(name = "institution_Id") var institutionId: Long? = null
) : Serializable