package de.vinz.openfls.domains.clients.projections

interface ClientSoloProjection {
        val id: Long
        val firstName: String
        val lastName: String
        val phoneNumber: String
        val email: String
}