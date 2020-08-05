package dev.saljuama.demo.nerdalert.accounts.registration

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