package de.vinz.openfls.security

import de.vinz.openfls.domains.authentication.UserRole
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.entity.EmployeeAccess
import de.vinz.openfls.domains.employees.service.EmployeeAccessService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CustomUserDetailsService(
        private val employeeAccessService: EmployeeAccessService,
        private val passwordEncoder: PasswordEncoder
): UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = employeeAccessService.getEntityByUsername(username)

        // user found
        if (user != null) {
            return CustomUserDetails(user)
        }

        // no users in db it will return default admin for initial progress
        if (!employeeAccessService.existsAnyAccess()) {
            return CustomUserDetails(getInitialAdminEmployeeAccess())
        }

        throw UsernameNotFoundException("Could not find user")
    }

    fun getInitialAdminEmployeeAccess(): EmployeeAccess {
        return EmployeeAccess(
                id = 0,
                username = "admin",
                password = passwordEncoder.encode("admin").orEmpty(),
                role = UserRole.ADMIN.id,
                employee = Employee(
                        id = 0,
                        firstname = "Initial",
                        lastname = "Administrator",
                        phonenumber = "",
                        email = "",
                        inactive = false,
                        description = "",
                        access = null,
                        permissions = mutableSetOf(),
                        unprofessionals = mutableSetOf(),
                        contingents = mutableSetOf(),
                        createdEvaluations = mutableSetOf(),
                        updatedEvaluations = mutableSetOf(),
                        services = mutableSetOf(),
                        assistancePlanFavorites = mutableSetOf()
                )
        )
    }
}
