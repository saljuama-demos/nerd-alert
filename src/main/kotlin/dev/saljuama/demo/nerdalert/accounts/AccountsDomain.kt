package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Either
import arrow.fx.IO
import dev.saljuama.demo.nerdalert.accounts.registration.NewAccount
import dev.saljuama.demo.nerdalert.accounts.registration.StarterAccount
import java.time.LocalDate

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

class AccountNotFoundException : Throwable()

interface AccountsService {
  fun createAccount(newAccount: NewAccount): Either<Throwable, StarterAccount>
  fun verifyAccount(username: String, token: String): Either<Throwable, Account>
  fun updateProfile(username: String, profile: UserProfile): Either<Throwable, Account>
  fun deleteAccount(username: String): Either<Throwable, Unit>
  fun listAllAccounts(): Either<Throwable, List<Account>>
  fun viewAccountDetails(username: String): Either<Throwable, Account>
}

interface AccountsRepository {
  fun saveAccount(account: NewAccount): IO<StarterAccount>
  fun findVerifiableAccount(username: String): IO<StarterAccount>
  fun verifyAccount(account: StarterAccount): IO<Unit>
  fun findVerifiedAccount(username: String): IO<Account>
  fun updateProfile(account: Account): IO<Unit>
  fun deleteAccount(username: String): IO<Unit>
}
