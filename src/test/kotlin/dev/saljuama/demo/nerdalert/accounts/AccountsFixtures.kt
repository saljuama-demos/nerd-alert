package dev.saljuama.demo.nerdalert.accounts

import java.time.LocalDate

object AccountsFixtures {
  val username = "user1"
  val email = "email@one.com"
  val password = "pass1"
  val registeredAt = LocalDate.of(2020, 1, 1)
  val verificationToken = "verification-token"

  fun newAccount(): NewAccount = NewAccount(username, email, password)
  fun starterAccount(): StarterAccount = StarterAccount(username, email, registeredAt, AccountVerification(verificationToken))
  fun account(): Account = Account(username, email, registeredAt)
}
