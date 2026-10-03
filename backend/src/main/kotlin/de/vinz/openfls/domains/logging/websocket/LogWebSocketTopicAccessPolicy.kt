package de.vinz.openfls.domains.logging.websocket

import de.vinz.openfls.websocket.StompTopicAccessPolicy
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class LogWebSocketTopicAccessPolicy : StompTopicAccessPolicy {
    override val destination = LogWebSocketTopic.LIVE_ENTRIES
    override fun isAllowed(authentication: Authentication) = authentication.authorities.any { it.authority == "ADMIN" }
}
