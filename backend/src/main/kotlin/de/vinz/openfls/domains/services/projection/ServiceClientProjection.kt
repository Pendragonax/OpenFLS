package de.vinz.openfls.domains.services.projection

interface ServiceClientProjection {
    val id: Long
    val firstName: String
    val lastName: String
    val phoneNumber: String
    val email: String
    val archived: Boolean
}
