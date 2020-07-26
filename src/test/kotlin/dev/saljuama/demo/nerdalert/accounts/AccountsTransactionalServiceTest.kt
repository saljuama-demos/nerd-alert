package dev.saljuama.demo.nerdalert.accounts

import arrow.fx.IO
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.account
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.newAccount
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.starterAccount
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.userProfile
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.username
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.verificationToken
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class AccountsTransactionalServiceTest {

  @MockK
  private lateinit var repository: AccountsRepository

  private lateinit var service: AccountsService

  @BeforeEach
  fun setUp() {
    service = AccountsTransactionalService(repository)
  }

  @Test
  fun `create a new account returns a starter account with a verification token`() {
    val newAccount = newAccount()
    every { repository.saveAccount(newAccount) } returns IO { starterAccount() }

    val result = service.createAccount(newAccount)

    assertTrue(result.isRight())
    result.map { starterAccount -> assertNotNull(starterAccount.verification.token) }
  }

  @Test
  fun `create a new account when username and-or email are already in use returns an exception`() {
    val newAccount = newAccount()
    every { repository.saveAccount(newAccount) } returns IO { throw UsernameOrEmailNotAvailableException() }

    val result = service.createAccount(newAccount)

    assertTrue(result.isLeft())
  }

  @Test
  fun `verifying an account when is an starter account returns the verified account without user profile`() {
    val starterAccount = starterAccount()
    every { repository.findVerifiableAccount(username) } returns IO { starterAccount }
    every { repository.verifyAccount(starterAccount) } returns IO.unit

    val result = service.verifyAccount(username, verificationToken)

    assertTrue(result.isRight())
    result.map { account -> assertNull(account.profile) }
    verify { repository.verifyAccount(starterAccount) }
  }

  @Test
  fun `verifying an account with an invalid token returns an exception`() {
    every { repository.findVerifiableAccount(username) } returns IO { starterAccount() }

    val result = service.verifyAccount(username, "invalid-token")

    assertTrue(result.isLeft())
  }

  @Test
  fun `verifying an account that does not exist returns an error`() {
    every { repository.findVerifiableAccount(username) } returns IO { throw AccountNotFoundException() }

    val result = service.verifyAccount(username, verificationToken)

    assertTrue(result.isLeft())
  }

  @Test
  fun `updating a profile for an existing account returns the account with the updated profile`() {
    every { repository.findVerifiedAccount(username) } returns IO { account() }
    every { repository.updateProfile(any()) } returns IO.unit

    val newProfile = userProfile()
    val result = service.updateProfile(username, newProfile)

    assertTrue(result.isRight())
    result.map { account -> assertEquals(newProfile, account.profile) }
  }

  @Test
  fun `updating a profile for a non-existing or non-validated account returns an error`() {
    every { repository.findVerifiedAccount(username) } returns IO { throw AccountNotFoundException() }

    val result = service.updateProfile(username, userProfile())

    assertTrue(result.isLeft())
  }

  @Test
  fun `delete an account that exists succeeds`() {
    every { repository.deleteAccount(username) } returns IO.unit

    val result = service.deleteAccount(username)

    assertTrue(result.isRight())
  }

  @Test
  fun `delete a non existing account returns an error`() {
    every { repository.deleteAccount(username) } returns IO { throw AccountNotFoundException() }

    val result = service.deleteAccount(username)

    assertTrue(result.isLeft())
  }

}