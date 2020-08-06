package dev.saljuama.demo.nerdalert.accounts.registration

import arrow.core.Either
import arrow.fx.IO
import dev.saljuama.demo.nerdalert.accounts.Account
import java.time.LocalDate
import java.util.*

data class NewAccount(
  val username: String,
  val email: String,
  val password: String
)

data class StarterAccount(
  val username: String,
  val email: String,
  val registered: LocalDate = LocalDate.now(),
  val verification: AccountVerification = AccountVerification()
)

data class AccountVerification(
  val token: String = UUID.randomUUID().toString()
)

class UsernameOrEmailNotAvailableException : Throwable()
class InvalidVerificationException : Throwable()

interface AccountRegistrationService {
  fun createAccount(newAccount: NewAccount): Either<Throwable, StarterAccount>
  fun verifyAccount(username: String, token: String): Either<Throwable, Account>
}

interface AccountRegistrationRepository {
  fun saveAccount(account: NewAccount): IO<StarterAccount>
  fun findVerifiableAccount(username: String): IO<StarterAccount>
  fun verifyAccount(account: StarterAccount): IO<Unit>
}
