package dev.saljuama.demo.nerdalert.accounts

import arrow.core.getOrElse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class AccountsRequestHandler(
  val accountsRepository: AccountsRepository
) {
  val log: Logger = LoggerFactory.getLogger(AccountsRequestHandler::class.java)

  data class NewAccountRequest(val username: String, val email: String, val password: String) {
    fun toEntity(): AccountEntity {
      return AccountEntity(null, this.username, this.email, this.password)
    }
  }

  data class NewAccountResponse(val verificationUrl: String)

  data class NewAccountError(val error: String = "username and/or email not available")

  fun registerNewAccount(request: ServerRequest): ServerResponse {
    val newAccountRequest = request.body(NewAccountRequest::class.java)
    log.info("We've got a new account! $newAccountRequest")

    return accountsRepository.createNewAccount(newAccountRequest.toEntity())
      .map {
        val username: String = it.username
        val token: String = it.verification?.token!!
        val responseBody = NewAccountResponse("http://localhost:8080/api/accounts/$username/verify/$token")
        ServerResponse.status(HttpStatus.CREATED).body(responseBody)
      }
      .getOrElse { ServerResponse.status(HttpStatus.BAD_REQUEST).body(NewAccountError()) }
  }

  fun verifyNewAccount(request: ServerRequest): ServerResponse {
    val token = request.pathVariable("token")
    val username = request.pathVariable("username")

    return accountsRepository.verifyNewAccount(username, token)
      .map { ServerResponse.ok().build() }
      .getOrElse { ServerResponse.badRequest().build() }
  }
}
