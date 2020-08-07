package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Either
import arrow.fx.IO
import java.time.LocalDate

data class Account(
  val username: String,
  val email: String,
  val registered: LocalDate = LocalDate.now(),
  val profile: UserProfile = UserProfile(username)
)

data class UserProfile(
  val firstName: String,
  val lastName: String? = null,
  val description: String? = null,
  val imageUrl: String? = null
)

class AccountNotFoundException : Throwable()

interface AccountsService {
  fun updateProfile(username: String, profile: UserProfile): Either<Throwable, Account>
  fun deleteAccount(username: String): Either<Throwable, Unit>
  fun listAllAccounts(): Either<Throwable, List<Account>>
}

interface AccountsRepository {
  fun findVerifiedAccount(username: String): IO<Account>
  fun updateProfile(account: Account): IO<Unit>
  fun deleteAccount(username: String): IO<Unit>
}
