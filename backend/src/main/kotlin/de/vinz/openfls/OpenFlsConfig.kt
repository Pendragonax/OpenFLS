package de.vinz.openfls

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
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey


@Configuration
@EnableWebSecurity
class OpenFlsConfig : WebSecurityConfigurerAdapter() {

    @Value("\${jwt.private-key}")
    private val rsaPrivateKey: RSAPrivateKey? = null

    @Value("\${jwt.public-key}")
    private val rsaPublicKey: RSAPublicKey? = null

    @Bean
    public override fun userDetailsService(): org.springframework.security.core.userdetails.UserDetailsService =
        de.vinz.openfls.services.UserDetailsService()

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        return DaoAuthenticationProvider().apply {
            setUserDetailsService(userDetailsService())
            setPasswordEncoder(passwordEncoder())
        }
    }

    @Bean
    fun modelMapper(): ModelMapper? = ModelMapper()

    @Bean
    fun passwordEncoder(): PasswordEncoder? = BCryptPasswordEncoder()

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth?.authenticationProvider(authenticationProvider())
    }

    override fun configure(http: HttpSecurity?) {
        http!!.csrf().disable().cors()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(BearerTokenAuthenticationEntryPoint())
            .accessDeniedHandler(BearerTokenAccessDeniedHandler())
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/login/**").permitAll()
            .antMatchers("/change_role/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.POST,"/employees/assistance_plan/**").authenticated()
            .antMatchers(HttpMethod.PUT,"/employees/assistance_plan/**").authenticated()
            .antMatchers(HttpMethod.DELETE,"/employees/assistance_plan/**").authenticated()
            .antMatchers(HttpMethod.POST,"/employees/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.PUT,"/employees/**").hasAnyAuthority("ADMIN", "LEAD")
            .antMatchers(HttpMethod.DELETE,"/employees/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.POST,"/permissions/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.PUT,"/permissions/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.DELETE,"/permissions/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.POST,"/categories/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.PUT,"/categories/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.DELETE,"/categories/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.POST,"/hour_types/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.PUT,"/hour_types/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.DELETE,"/hour_types/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.POST,"/institutions/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.PUT,"/institutions/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.DELETE,"/institutions/**").hasAuthority("ADMIN")
            .antMatchers("/contingents/**").hasAnyAuthority("ADMIN", "LEAD")
            .antMatchers(HttpMethod.POST,"/sponsors/**").hasAnyAuthority("ADMIN", "LEAD")
            .antMatchers(HttpMethod.PUT,"/sponsors/**").hasAnyAuthority("ADMIN", "LEAD")
            .antMatchers(HttpMethod.DELETE,"/sponsors/**").hasAnyAuthority("ADMIN", "LEAD")
            .anyRequest().authenticated()
            .and().httpBasic(Customizer.withDefaults())
            .oauth2ResourceServer()
            .jwt()
    }

    @Bean
    fun jwtEncoder(): JwtEncoder? {
        val jwk: JWK = RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).build()
        val jwks: JWKSource<SecurityContext> = ImmutableJWKSet(JWKSet(jwk))
        return NimbusJwtEncoder(jwks)
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
            addAllowedOriginPattern("*");
            addAllowedHeader("*");
            addAllowedMethod("OPTIONS");
            addAllowedMethod("HEAD");
            addAllowedMethod("GET");
            addAllowedMethod("PUT");
            addAllowedMethod("POST");
            addAllowedMethod("DELETE");
            addAllowedMethod("PATCH");
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }

        return CorsFilter(source)
    }

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager? = super.authenticationManagerBean()
}