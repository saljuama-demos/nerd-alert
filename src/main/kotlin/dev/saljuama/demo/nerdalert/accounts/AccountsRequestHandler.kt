package dev.saljuama.demo.nerdalert.accounts

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

  fun registerNewAccount(request: ServerRequest): ServerResponse {
    val newAccountRequest = request.body(NewAccountRequest::class.java)
    log.info("We've got a new account! $newAccountRequest")

    return try {
      val savedAccount = accountsRepository.createNewAccount(newAccountRequest.toEntity())
      val username: String = savedAccount.username
      val token: String = savedAccount.verification?.token!!
      val responseBody = NewAccountResponse("http://localhost:8080/api/account/$username/$token")
      ServerResponse.status(HttpStatus.CREATED).body(responseBody)
    } catch (e: Exception) {
      ServerResponse.status(500).body("Could not create the new account!")
    }

  }

  fun verifyNewAccount(request: ServerRequest): ServerResponse {
    val token = request.pathVariable("token")
    val username = request.pathVariable("username")

    return when (accountsRepository.verifyNewAccount(username, token)) {
      true -> ServerResponse.ok().build()
      false -> ServerResponse.badRequest().build()
    }
  }
}
