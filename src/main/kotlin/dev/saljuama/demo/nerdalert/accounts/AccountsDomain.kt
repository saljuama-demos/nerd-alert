package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Either
import arrow.fx.IO
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

data class Account(
  val username: String,
  val email: String,
  val registered: LocalDate = LocalDate.now(),
  val profile: UserProfile? = null
)

data class UserProfile(
  val firstName: String,
  val lastName: String? = null,
  val description: String? = null,
  val imageUrl: String? = null
)

class UsernameOrEmailNotAvailableException : Throwable()
class InvalidVerificationException : Throwable()
class AccountNotVerifiedException : Throwable()
class AccountNotFoundException : Throwable()

interface AccountsService {
  fun createAccount(newAccount: NewAccount): Either<Throwable, StarterAccount>
  fun verifyAccount(username: String, token: String): Either<Throwable, Account>
  fun updateProfile(username: String, profile: UserProfile): Either<Throwable, Account>
  fun deleteAccount(username: String): Either<Throwable, Unit>
}

interface AccountsRepository {
  fun saveAccount(account: NewAccount): IO<StarterAccount>
  fun findVerifiableAccount(username: String): IO<StarterAccount>
  fun verifyAccount(account: StarterAccount): IO<Unit>
  fun findVerifiedAccount(username: String): IO<Account>
  fun updateProfile(account: Account): IO<Unit>
  fun deleteAccount(username: String): IO<Unit>
}
