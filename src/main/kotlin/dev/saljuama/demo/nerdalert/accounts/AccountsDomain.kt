package dev.saljuama.demo.nerdalert.accounts

import java.time.LocalDate

data class Account(
  val username: String,
  val email: String,
  val password: String,
  val registered: LocalDate = LocalDate.now(),
  val verified: Boolean = false,
  val verification: AccountVerification? = null,
  val profile: UserProfile? = null
)

data class AccountVerification(
  val token: String
)

data class UserProfile(
  val firstName: String,
  val lastName: String?,
  val description: String?,
  val imageUrl: String?
)

class UsernameOrEmailNotAvailableException : Throwable()
class InvalidVerificationTokenException : Throwable()
class AccountNotVerifiedException : Throwable()
class AccountNotFoundException : Throwable()
