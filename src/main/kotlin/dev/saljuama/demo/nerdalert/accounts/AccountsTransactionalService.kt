package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Either
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AccountsTransactionalService(
  private val repository: AccountsRepository
) : AccountsService {

  @Transactional
  override fun deleteAccount(username: String): Either<Throwable, Unit> {
    return repository.deleteAccount(username)
      .attempt()
      .unsafeRunSync()
  }

  override fun listAllAccounts(): Either<Throwable, List<Account>> {
    TODO("not implemented")
  }
}
