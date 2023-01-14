package de.vinz.openfls.services

import de.vinz.openfls.model.CustomUserDetails
import de.vinz.openfls.model.Employee
import de.vinz.openfls.model.EmployeeAccess
import de.vinz.openfls.repositories.EmployeeAccessRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

class UserDetailsService(): UserDetailsService {

    @Autowired
    private var employeeAccessRepository: EmployeeAccessRepository? = null

    @Autowired
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    override fun loadUserByUsername(username: String?): UserDetails {
        val user = employeeAccessRepository?.getEmployeeByUsername(username!!)

        if (user == null) {
            val userCount = employeeAccessRepository?.count()
            if (userCount == 0L) {
                return CustomUserDetails(
                    EmployeeAccess(
                        id = 0,
                        username = "admin",
                        password = passwordEncoder().encode("admin"),
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
                            permissions = HashSet(),
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