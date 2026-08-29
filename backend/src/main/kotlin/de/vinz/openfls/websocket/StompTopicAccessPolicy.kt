package de.vinz.openfls.websocket

import org.springframework.security.core.Authentication

/** Domain-owned policies declare who may subscribe to their STOMP topic. */
interface StompTopicAccessPolicy {
    val destination: String
    fun isAllowed(authentication: Authentication): Boolean
}
