package dev.saljuama.demo.nerdalert.accounts

import arrow.core.getOrHandle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

data class AccountSummary(val username: String, val descriptionLink: String)

data class ErrorResponse(val error: String)

data class NewUserProfileRequest(
  val firstName: String,
  val lastName: String?,
  val description: String?,
  val imageUrl: String?
) {
  fun toInput() = UserProfile(firstName, lastName, description, imageUrl)
}

@Component
class AccountsRequestHandler(
  private val accountsService: AccountsService
) {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun upsertProfile(request: ServerRequest): ServerResponse {
    val username = request.pathVariable("username")
    val newUserProfileRequest = request.body(NewUserProfileRequest::class.java)

    return accountsService.updateProfile(username, newUserProfileRequest.toInput())
      .map { ServerResponse.status(HttpStatus.CREATED).build() }
      .getOrHandle { error ->
        when (error) {
          is AccountNotFoundException -> ServerResponse.badRequest().body(ErrorResponse("account not found or not verified"))
          else -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse("unknown error"))
        }
      }
  }

  fun listAccounts(request: ServerRequest): ServerResponse {
    return accountsService.listAllAccounts()
      .map { accounts -> accounts.map { account -> AccountSummary(account.username, "http://localhost:8080/api/accounts/${account.username}") } }
      .map { accounts -> ServerResponse.ok().body(accounts) }
      .getOrHandle { error ->
        when (error) {
          is AccountNotFoundException -> ServerResponse.notFound().build()
          else -> ServerResponse.status(500).build()
        }
      }
  }

  fun viewAccountDetails(request: ServerRequest): ServerResponse {
    val username = request.pathVariable("username")
    return accountsService.viewAccountDetails(username)
      .map { account -> ServerResponse.ok().body(account) }
      .getOrHandle { error ->
        when (error) {
          is AccountNotFoundException -> ServerResponse.notFound().build()
          else -> ServerResponse.status(500).build()
        }
      }
  }
}
