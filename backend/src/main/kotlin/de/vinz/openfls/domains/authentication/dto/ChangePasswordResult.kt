package de.vinz.openfls.domains.authentication.dto

sealed class ChangePasswordResult {
    data object Success : ChangePasswordResult()
    data object EmployeeNotFound : ChangePasswordResult()
    data object WrongOldPassword : ChangePasswordResult()
}
