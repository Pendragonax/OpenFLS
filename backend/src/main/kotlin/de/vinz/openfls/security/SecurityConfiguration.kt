package de.vinz.openfls.security

import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Value("\${jwt.private-key}")
    private val rsaPrivateKey: RSAPrivateKey? = null

    @Value("\${jwt.public-key}")
    private val rsaPublicKey: RSAPublicKey? = null

    @Bean
    fun authenticationProvider(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder
    ): DaoAuthenticationProvider {
        return DaoAuthenticationProvider(userDetailsService).apply {
            setPasswordEncoder(passwordEncoder)
        }
    }

    @Bean
    fun modelMapper(): ModelMapper? = ModelMapper()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { csrf -> csrf.disable() }

        http.sessionManagement { mgm -> mgm.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        http.authorizeHttpRequests { auth ->
            run {
                auth.requestMatchers(HttpMethod.POST, "/login/**").permitAll()
                        .requestMatchers("/change_role/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/employees/assistance_plan/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/employees/assistance_plan/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/employees/assistance_plan/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/employees/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/employees/**").hasAnyAuthority("ADMIN", "LEAD")
                        .requestMatchers(HttpMethod.DELETE, "/employees/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/permissions/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/permissions/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/permissions/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/hour_types/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/hour_types/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/hour_types/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/institutions/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/institutions/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/institutions/**").hasAuthority("ADMIN")
                        .requestMatchers("/contingents/**").hasAnyAuthority("ADMIN", "LEAD")
                        .requestMatchers(HttpMethod.POST, "/sponsors/**").hasAnyAuthority("ADMIN", "LEAD")
                        .requestMatchers(HttpMethod.PUT, "/sponsors/**").hasAnyAuthority("ADMIN", "LEAD")
                        .requestMatchers(HttpMethod.DELETE, "/sponsors/**").hasAnyAuthority("ADMIN", "LEAD")
                        .anyRequest().authenticated()
            }
        }

        http.oauth2ResourceServer { oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(BearerTokenAccessDeniedHandler())
        }

        return http.build()
    }

    @Bean
    fun jwtEncoder(): JwtEncoder? {
        val jwk: JWK = RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).build()
        val jwkSource: JWKSource<SecurityContext> = ImmutableJWKSet(JWKSet(jwk))
        return NimbusJwtEncoder(jwkSource)
    }

    @Bean
    fun jwtDecoder(): JwtDecoder? = NimbusJwtDecoder.withPublicKey(rsaPublicKey).build()

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter? {
        val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter().apply {
            setAuthoritiesClaimName("roles")
            setAuthorityPrefix("")
        }

        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter)
        }
    }

    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration().apply {
            allowCredentials = true
            addAllowedOriginPattern("*")
            addAllowedHeader("*")
            addAllowedMethod("OPTIONS")
            addAllowedMethod("HEAD")
            addAllowedMethod("GET")
            addAllowedMethod("PUT")
            addAllowedMethod("POST")
            addAllowedMethod("DELETE")
            addAllowedMethod("PATCH")
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }

        return CorsFilter(source)
    }
}
