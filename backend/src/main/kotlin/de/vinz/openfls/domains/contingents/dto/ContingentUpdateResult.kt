package de.vinz.openfls.domains.contingents.dto

sealed class ContingentUpdateResult {
    data class Success(val response: ContingentResponse) : ContingentUpdateResult()
    data object NotFound : ContingentUpdateResult()
    data class InvalidRange(val message: String) : ContingentUpdateResult()
    data class EmployeeNotFound(val message: String) : ContingentUpdateResult()
    data class EmployeeArchived(val message: String) : ContingentUpdateResult()
    data class InstitutionNotFound(val message: String) : ContingentUpdateResult()
}
