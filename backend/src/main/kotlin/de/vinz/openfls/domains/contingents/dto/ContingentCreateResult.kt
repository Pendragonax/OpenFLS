package de.vinz.openfls.domains.contingents.dto

sealed class ContingentCreateResult {
    data class Success(val response: ContingentResponse) : ContingentCreateResult()
    data class InvalidRange(val message: String) : ContingentCreateResult()
    data class EmployeeNotFound(val message: String) : ContingentCreateResult()
    data class EmployeeArchived(val message: String) : ContingentCreateResult()
    data class InstitutionNotFound(val message: String) : ContingentCreateResult()
}
