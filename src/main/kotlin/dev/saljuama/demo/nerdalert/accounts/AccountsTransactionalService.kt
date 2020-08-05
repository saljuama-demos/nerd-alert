package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Either
import arrow.fx.extensions.io.async.effectMap
import arrow.fx.extensions.io.monad.flatTap
import dev.saljuama.demo.nerdalert.accounts.registration.*
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AccountsTransactionalService(
  val repository: AccountsRepository
) : AccountsService {

  @Transactional
  override fun createAccount(newAccount: NewAccount): Either<Throwable, StarterAccount> {
    return repository.saveAccount(newAccount)
      .attempt()
      .unsafeRunSync()
  }

  @Transactional
  override fun verifyAccount(username: String, token: String): Either<Throwable, Account> {
    return repository.findVerifiableAccount(username)
      .effectMap { starterAccount -> validateToken(token, starterAccount) }
      .flatTap { starterAccount -> repository.verifyAccount(starterAccount) }
      .map { starterAccount -> Account(starterAccount.username, starterAccount.email, starterAccount.registered) }
      .attempt()
      .unsafeRunSync()
  }

  private fun validateToken(token: String, starterAccount: StarterAccount): StarterAccount {
    if (token != starterAccount.verification.token)
      throw InvalidVerificationException()
    return starterAccount
  }

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
