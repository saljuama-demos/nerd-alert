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

  fun registerNewAccount(request: ServerRequest): ServerResponse {
    val newAccountRequest = request.body(NewAccountRequest::class.java)
    log.info("We've got a new account! $newAccountRequest")

    return when (accountsRepository.createNewAccount(newAccountRequest.toEntity())) {
      true -> ServerResponse.status(HttpStatus.CREATED).build()
      false -> ServerResponse.status(500).body("Could not create the new account!")
    }
  }
}
