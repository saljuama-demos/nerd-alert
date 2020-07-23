package dev.saljuama.demo.nerdalert.accounts

import arrow.core.getOrElse
import arrow.core.getOrHandle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

data class NewAccountRequest(
  val username: String,
  val email: String,
  val password: String
) {
  fun toNewAccount() = NewAccount(username, email, password)
}

data class NewAccountResponse(val verificationUrl: String)

data class ErrorResponse(val error: String)

data class NewUserProfileRequest(
  val firstName: String,
  val lastName: String?,
  val description: String?,
  val imageUrl: String?
) {
  fun toInput(username: String) = UserProfileInput(username, firstName, lastName, description, imageUrl)
}


class AccountsRequestHandler(
  val accountsService: AccountsService
) {
  val log: Logger = LoggerFactory.getLogger(AccountsRequestHandler::class.java)

  fun registerNewAccount(request: ServerRequest): ServerResponse {
    val newAccountRequest = request.body(NewAccountRequest::class.java)
    log.info("We've got a new account! $newAccountRequest")

    return accountsService.createNewAccount(newAccountRequest.toNewAccount())
      .map {
        val username: String = it.username
        val token: String = it.verification?.token!!
        val responseBody = NewAccountResponse("http://localhost:8080/api/accounts/$username/verify/$token")
        ServerResponse.status(HttpStatus.CREATED).body(responseBody)
      }
      .getOrElse { ServerResponse.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("username and/or email not available")) }
  }

  fun verifyNewAccount(request: ServerRequest): ServerResponse {
    val token = request.pathVariable("token")
    val username = request.pathVariable("username")

    return accountsService.verifyNewAccount(username, token)
      .map { ServerResponse.ok().build() }
      .getOrElse { ServerResponse.badRequest().build() }
  }

  fun createProfile(request: ServerRequest): ServerResponse {
    val username = request.pathVariable("username")
    val newUserProfileRequest = request.body(NewUserProfileRequest::class.java)

    return accountsService.createUserProfile(newUserProfileRequest.toInput(username))
      .map { ServerResponse.status(HttpStatus.CREATED).build() }
      .getOrHandle { error ->
        when (error) {
          is AccountNotVerifiedException -> ServerResponse.badRequest().body(ErrorResponse("account not verified"))
          is AccountNotFoundException -> ServerResponse.badRequest().body(ErrorResponse("account not found"))
          else -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse("unknown error"))
        }
      }
  }
}
