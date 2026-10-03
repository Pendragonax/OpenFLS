package de.vinz.openfls.domains.employees.dto

sealed class EmployeeArchiveResult {
    data class Success(val response: EmployeeArchiveHistoryEntryResponse) : EmployeeArchiveResult()
    data object NotFound : EmployeeArchiveResult()
    data object ActorNotFound : EmployeeArchiveResult()
    data object AlreadyArchived : EmployeeArchiveResult()
    data object NotArchived : EmployeeArchiveResult()
}
