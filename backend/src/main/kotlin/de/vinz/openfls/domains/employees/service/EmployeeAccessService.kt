package de.vinz.openfls.domains.employees.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.authentication.UserRole
import de.vinz.openfls.domains.employees.entity.EmployeeAccess
import de.vinz.openfls.domains.employees.repository.EmployeeAccessRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeAccessService(
    private val employeeAccessRepository: EmployeeAccessRepository
) {

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityByUsername(username: String): EmployeeAccess? {
        return employeeAccessRepository.findByUsername(username)
    }

    @Transactional(readOnly = true)
    fun existsByUsername(username: String): Boolean {
        return employeeAccessRepository.findByUsername(username) != null
    }

    @Transactional(readOnly = true)
    fun existsAnyAccess(): Boolean {
        return employeeAccessRepository.count() > 0
    }

    @Transactional(readOnly = true)
    fun getPasswordHashById(id: Long): String? {
        return employeeAccessRepository.findByIdOrNull(id)?.password
    }

    @Transactional
    fun changePassword(id: Long, encodedPassword: String): Boolean {
        return employeeAccessRepository.changePassword(id, encodedPassword) > 0
    }

    @Transactional(readOnly = true)
    fun isAdminById(id: Long): Boolean {
        return employeeAccessRepository.findByIdOrNull(id)?.role == UserRole.ADMIN.id
    }
}
