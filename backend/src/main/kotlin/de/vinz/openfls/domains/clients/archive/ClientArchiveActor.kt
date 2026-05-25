package de.vinz.openfls.domains.clients.archive

data class ClientArchiveActor(
    val employeeId: Long,
    val firstname: String,
    val lastname: String,
    val isAdmin: Boolean,
    val leadingInstitutionIds: List<Long>
)
