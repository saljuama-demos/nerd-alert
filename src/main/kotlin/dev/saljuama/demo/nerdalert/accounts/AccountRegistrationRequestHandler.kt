package dev.saljuama.demo.nerdalert.accounts

import arrow.core.getOrElse
import arrow.core.getOrHandle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

data class NewAccountResponse(val verificationUrl: String)

@Component
class AccountRegistrationRequestHandler(
  private val accountRegistrationService: AccountRegistrationService
) {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun registerNewAccount(request: ServerRequest): ServerResponse {
    val newAccountRequest = request.body(NewAccount::class.java)
    log.info("We've got a new account! $newAccountRequest")

    return accountRegistrationService.createAccount(newAccountRequest)
      .map {
        val username: String = it.username
        val token: String = it.verification.token
        val responseBody = NewAccountResponse("http://localhost:8080/api/accounts/$username/verify/$token")
        ServerResponse.status(HttpStatus.CREATED).body(responseBody)
      }
      .getOrElse { ServerResponse.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("username and/or email not available")) }
  }

  fun verifyStarterAccount(request: ServerRequest): ServerResponse {
    val token = request.pathVariable("token")
    val username = request.pathVariable("username")
    return accountRegistrationService.verifyAccount(username, token)
      .map { ServerResponse.ok().build() }
      .getOrElse { ServerResponse.badRequest().build() }
  }

  fun deleteAccount(request: ServerRequest): ServerResponse {
    val username = request.pathVariable("username")
    return accountRegistrationService.deleteAccount(username)
      .map { ServerResponse.noContent().build() }
      .getOrHandle { error ->
        when (error) {
          is AccountNotFoundException -> ServerResponse.noContent().build()
          else -> ServerResponse.status(500).build()
        }
      }
  }
}