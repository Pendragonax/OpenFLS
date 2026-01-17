package de.vinz.openfls.services

import de.vinz.openfls.domains.permissions.PermissionService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class UserService(
        private val permissionService: PermissionService
) {

    fun getUserId(): Long {
        val jwt = getJwt()
        val userId = jwt.getClaimAsString("id")
        return userId.toLong()
    }

    fun isAdmin(): Boolean {
        val jwt = getJwt()
        val userId = jwt.getClaimAsString("id").toLong()
        return permissionService.isAdminByUserId(userId)
    }

    fun getJwt(): Jwt {
        val authentication = SecurityContextHolder.getContext().authentication
                ?: throw IllegalStateException("No authentication present")
        return authentication.principal as Jwt
    }
}
