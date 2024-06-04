package de.vinz.openfls.security

import de.vinz.openfls.domains.authentication.models.EUserRoles
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(private var employeeAccess: EmployeeAccess) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        val roleString = when (employeeAccess.role) {
            1 -> EUserRoles.ADMIN.name
            2 -> EUserRoles.LEAD.name
            else -> EUserRoles.USER.name
        }

        return mutableListOf(SimpleGrantedAuthority(roleString))
    }

    override fun getPassword(): String = employeeAccess.password

    override fun getUsername(): String = employeeAccess.username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    fun getId(): Long = employeeAccess.id ?: -1
}