package de.vinz.openfls.services

import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val jwtDecoder: JwtDecoder
) {
    /**
     * Decode the token and extracts the id and the admin status.
     * @param token = jwt-token
     * @return [Pair] with the id and weather the user is an administrator
     */
    fun getUserInfo(token: String) : Pair<Long, Boolean> {
        val decodedToken = jwtDecoder
            .decode(token.replace("Bearer ", ""))
        return Pair(
            decodedToken.getClaimAsString("id").toLong(),
            decodedToken.getClaimAsString("roles").toString() == "ADMIN"
        )
    }
}