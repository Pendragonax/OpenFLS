package de.vinz.openfls.security

import de.vinz.openfls.domains.authentication.models.EUserRoles
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import de.vinz.openfls.domains.employees.EmployeeAccessRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CustomUserDetailsService(
        private val employeeAccessRepository: EmployeeAccessRepository,
        private val passwordEncoder: PasswordEncoder
): UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        val user = employeeAccessRepository.getEmployeeByUsername(username!!)

        // user found
        if (user != null) {
            return CustomUserDetails(user)
        }

        // no users in db it will return default admin for initial progress
        if (employeeAccessRepository.count() == 0L) {
            return CustomUserDetails(getInitialAdminEmployeeAccess())
        }

        throw UsernameNotFoundException("Could not find user")
    }

    fun getInitialAdminEmployeeAccess(): EmployeeAccess {
        return EmployeeAccess(
                id = 0,
                username = "admin",
                password = passwordEncoder.encode("admin"),
                role = EUserRoles.ADMIN.id,
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