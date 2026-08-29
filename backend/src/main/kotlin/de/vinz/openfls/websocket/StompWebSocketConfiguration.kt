package de.vinz.openfls.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class StompWebSocketConfiguration(
    private val jwtDecoder: JwtDecoder,
    private val topicAccessPolicies: List<StompTopicAccessPolicy>
) : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic", "/queue")
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object : ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
                val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java) ?: return message
                if (accessor.command == StompCommand.CONNECT) {
                    val rawToken = accessor.getFirstNativeHeader("Authorization")?.removePrefix("Bearer ") ?: return null
                    val jwt = runCatching { jwtDecoder.decode(rawToken) }.getOrNull() ?: return null
                    val authorities = (jwt.getClaimAsStringList("roles") ?: emptyList()).map(::SimpleGrantedAuthority)
                    accessor.user = UsernamePasswordAuthenticationToken(jwt.subject, null, authorities)
                }
                if (accessor.command == StompCommand.SUBSCRIBE) {
                    val authentication = accessor.user as? Authentication ?: return null
                    val policy = topicAccessPolicies.firstOrNull { it.destination == accessor.destination } ?: return null
                    if (!policy.isAllowed(authentication)) return null
                }
                return message
            }
        })
    }
}
