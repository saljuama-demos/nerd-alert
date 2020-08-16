package dev.saljuama.demo.nerdalert

import arrow.core.*
import io.jsonwebtoken.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*
import javax.crypto.spec.SecretKeySpec
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.xml.bind.DatatypeConverter


@Configuration
class Security(private val jwtTokenFactory: JwtTokenFactory) : WebSecurityConfigurerAdapter() {

  @Bean
  fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

  override fun configure(auth: AuthenticationManagerBuilder) {
    val passwordEncoder = passwordEncoder()
    auth
      .inMemoryAuthentication()
      .passwordEncoder(passwordEncoder)
      .withUser("user1").password(passwordEncoder.encode("secret")).roles("USER")
  }

  override fun configure(http: HttpSecurity) {
    http
      .csrf()
      .disable()
      .authorizeRequests()
      .antMatchers("/api/accounts/*/verify/*").anonymous()
      .antMatchers("/api/accounts").anonymous()
      .antMatchers(GET, "/api/profiles/*").anonymous()
      .antMatchers("/login").permitAll()
      .anyRequest().authenticated()
      .and()
      .addFilter(JwtLoginFilter(authenticationManager(), jwtTokenFactory))
      .addFilter(JwtAuthenticationFilter(authenticationManager(), jwtTokenFactory))
  }

}


@Component
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
  var secret: String = "",
  var tokenTimeoutInSeconds: Int = 1800
)


@Component
class JwtTokenFactory(private val jwtProperties: JwtProperties) {
  val signingKey: Key = SecretKeySpec(
    DatatypeConverter.parseBase64Binary(jwtProperties.secret),
    SignatureAlgorithm.HS256.jcaName
  )

  fun generateTokenForUser(username: String): String {
    val nowInMillis = System.currentTimeMillis()
    val expirationTimeInMillis = nowInMillis + (jwtProperties.tokenTimeoutInSeconds * 1000)
    return Jwts.builder()
      .setSubject(username)
      .setIssuedAt(Date(nowInMillis))
      .setExpiration(Date(expirationTimeInMillis))
      .signWith(signingKey)
      .compact()
  }

  fun parseToken(token: String): Option<String> {
    return try {
      val tokenParser = Jwts.parserBuilder().setSigningKey(signingKey).build()
      val username = tokenParser.parseClaimsJws(token).body.subject
      Some(username)
    } catch (e: JwtException) {
      println("Failed to parse JWT token because ${e.message}")
      None
    }
  }

}


class JwtLoginFilter(
  private val authManager: AuthenticationManager,
  private val jwtTokenFactory: JwtTokenFactory
) : UsernamePasswordAuthenticationFilter() {

  override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
    val username = obtainUsername(request)
    val password = obtainPassword(request)
    val authorities = listOf<GrantedAuthority>()
    return authManager.authenticate(UsernamePasswordAuthenticationToken(username, password, authorities))
  }

  override fun successfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain, authResult: Authentication) {
    val token = jwtTokenFactory.generateTokenForUser((authResult.principal as UserDetails).username)
    response.addHeader("Authorization", "Bearer $token")
  }

}


class JwtAuthenticationFilter(
  authManager: AuthenticationManager,
  private val jwtTokenFactory: JwtTokenFactory
) : BasicAuthenticationFilter(authManager) {

  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    fun parseAuthentication(request: HttpServletRequest): Option<UsernamePasswordAuthenticationToken> {
      val token = request.getHeader("Authorization")
      if (token == null || !token.startsWith("Bearer "))
        return None

      return jwtTokenFactory.parseToken(token.replace("Bearer ", ""))
        .map { username -> UsernamePasswordAuthenticationToken(username, null, listOf()) }
    }
    parseAuthentication(request).map { auth -> SecurityContextHolder.getContext().authentication = auth }
    chain.doFilter(request, response)
  }

}
