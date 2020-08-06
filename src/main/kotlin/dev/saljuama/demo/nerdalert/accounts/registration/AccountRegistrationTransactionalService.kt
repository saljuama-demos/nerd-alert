package dev.saljuama.demo.nerdalert.accounts.registration

import arrow.core.Either
import arrow.fx.extensions.io.async.effectMap
import arrow.fx.extensions.io.monad.flatTap
import dev.saljuama.demo.nerdalert.accounts.Account
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AccountRegistrationTransactionalService(
  private val repository: AccountRegistrationRepository
) : AccountRegistrationService {

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
}
