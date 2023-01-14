package de.vinz.openfls.services

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthenticationFacade {
    fun getAuthentication(): Authentication {
        return SecurityContextHolder.getContext().authentication;
    }
}