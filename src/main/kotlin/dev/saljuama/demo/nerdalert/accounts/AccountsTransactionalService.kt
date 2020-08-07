package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Either
import org.springframework.stereotype.Component

@Component
class AccountsTransactionalService : AccountsService {

  override fun listAllAccounts(): Either<Throwable, List<Account>> {
    TODO("not implemented")
  }
}
