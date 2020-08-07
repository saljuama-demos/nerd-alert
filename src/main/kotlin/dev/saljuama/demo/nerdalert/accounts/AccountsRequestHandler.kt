package dev.saljuama.demo.nerdalert.accounts

import arrow.core.getOrHandle
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

data class AccountSummary(val username: String, val descriptionLink: String)

data class ErrorResponse(val error: String)

@Component
class AccountsRequestHandler(
  private val accountsService: AccountsService
) {
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

}
