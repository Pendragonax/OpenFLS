package de.vinz.openfls.domains.services.projection

interface ServiceEmployeeProjection {
    val id: Long
    val firstname: String
    val lastname: String
    val email: String
    val phonenumber: String
    val description: String
    val archived: Boolean
}
