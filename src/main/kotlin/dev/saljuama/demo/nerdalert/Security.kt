package dev.saljuama.demo.nerdalert

import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
class Security : WebSecurityConfigurerAdapter() {

  override fun configure(auth: AuthenticationManagerBuilder) {
    val passwordEncoder: PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
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
      .antMatchers("/api/**").permitAll()
      .anyRequest().authenticated()
      .and()
      .addFilter(JwtLoginFilter(authenticationManager()))
      .addFilter(JwtAuthenticationFilter(authenticationManager()))
  }
}

class JwtLoginFilter(authenticationManager: AuthenticationManager) : UsernamePasswordAuthenticationFilter() {
  override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
    return super.attemptAuthentication(request, response)
  }

  override fun successfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain, authResult: Authentication) {
    super.successfulAuthentication(request, response, chain, authResult)
  }
}

class JwtAuthenticationFilter(authenticationManager: AuthenticationManager) : BasicAuthenticationFilter(authenticationManager) {
  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    super.doFilterInternal(request, response, chain)
  }
}
