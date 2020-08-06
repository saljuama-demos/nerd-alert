package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Either
import arrow.fx.extensions.io.monad.flatTap
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AccountsTransactionalService(
  private val repository: AccountsRepository
) : AccountsService {

  @Transactional
  override fun updateProfile(username: String, profile: UserProfile): Either<Throwable, Account> {
    return repository.findVerifiedAccount(username)
      .map { account -> account.copy(profile = profile) }
      .flatTap { repository.updateProfile(it) }
      .attempt()
      .unsafeRunSync()
  }

  @Transactional
  override fun deleteAccount(username: String): Either<Throwable, Unit> {
    return repository.deleteAccount(username)
      .attempt()
      .unsafeRunSync()
  }

  override fun listAllAccounts(): Either<Throwable, List<Account>> {
    TODO("not implemented")
  }

  override fun viewAccountDetails(username: String): Either<Throwable, Account> {
    TODO("not implemented")
  }

}
