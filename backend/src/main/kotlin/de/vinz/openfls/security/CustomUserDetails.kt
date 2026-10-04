package de.vinz.openfls.security

import de.vinz.openfls.domains.authentication.UserRole
import de.vinz.openfls.domains.employees.entity.EmployeeAccess
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(private var employeeAccess: EmployeeAccess) : UserDetails {

    private var role: UserRole = getRole()

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority(role.name))
    }

    override fun getPassword(): String = employeeAccess.password

    override fun getUsername(): String = employeeAccess.username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = !(employeeAccess.employee?.inactive ?: true) &&
            !(employeeAccess.employee?.archived ?: true)

    fun getId(): Long = employeeAccess.id ?: -1

    fun getRole(): UserRole = when (employeeAccess.role) {
        1 -> UserRole.ADMIN
        2 -> UserRole.LEAD
        else -> UserRole.USER
    }
}
