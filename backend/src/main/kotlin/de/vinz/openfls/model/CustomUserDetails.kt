package de.vinz.openfls.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(private var employeeAccess: EmployeeAccess): UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        val roleString = when(employeeAccess.role) {
            1 -> "ADMIN"
            2 -> "LEAD"
            else -> "USER"
        }

        val authority = SimpleGrantedAuthority(roleString)
        return mutableListOf(authority)
    }

    override fun getPassword(): String = employeeAccess.password

    override fun getUsername(): String = employeeAccess.username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    fun getId(): Long = employeeAccess.id ?: -1
}