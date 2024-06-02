package de.vinz.openfls.services

import de.vinz.openfls.entities.CustomUserDetails
import de.vinz.openfls.entities.Employee
import de.vinz.openfls.entities.EmployeeAccess
import de.vinz.openfls.repositories.EmployeeAccessRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserDetailsService(
        private val employeeAccessRepository: EmployeeAccessRepository,
        private val passwordEncoder: PasswordEncoder
): UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        val user = employeeAccessRepository.getEmployeeByUsername(username!!)

        if (user == null) {
            val userCount = employeeAccessRepository.count()

            // no users in db it will return default admin for initial progress
            if (userCount == 0L) {
                return CustomUserDetails(
                    EmployeeAccess(
                        id = 0,
                        username = "admin",
                        password = passwordEncoder.encode("admin"),
                        role = 1,
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
                            contingents = mutableSetOf(),
                            unprofessionals = mutableSetOf()
                        ),
                    )
                )
            }

            throw UsernameNotFoundException("could not find user")
        }

        return CustomUserDetails(user)
    }
}