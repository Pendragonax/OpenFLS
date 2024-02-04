package de.vinz.openfls.entities

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class UnprofessionalKey(
    @Column(name = "employee_Id") var employeeId: Long? = null,
    @Column(name = "sponsor_Id") var sponsorId: Long? = null
) : Serializable