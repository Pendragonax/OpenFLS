package de.vinz.openfls.entities

import java.io.Serializable
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class UnprofessionalKey(
    @Column(name = "employee_Id") var employeeId: Long? = null,
    @Column(name = "sponsor_Id") var sponsorId: Long? = null
) : Serializable