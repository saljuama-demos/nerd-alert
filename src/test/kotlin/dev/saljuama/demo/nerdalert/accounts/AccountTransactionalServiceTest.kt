package dev.saljuama.demo.nerdalert.accounts

import arrow.fx.IO
import dev.saljuama.demo.nerdalert.accounts.AccountFixtures.starterAccount
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class AccountTransactionalServiceTest {

  @MockK private lateinit var repository: AccountRepository
  private lateinit var service: AccountService

  @BeforeEach
  internal fun setUp() {
    service = AccountTransactionalService(repository)
  }

  @Test
  internal fun `create a new account returns a starter account with a verification token`() {
    val newAccount = AccountFixtures.newAccount()
    every { repository.saveAccount(newAccount) } returns IO { AccountFixtures.starterAccount() }

    val result = service.createAccount(newAccount)

    assertTrue(result.isRight())
    result.map { starterAccount -> assertNotNull(starterAccount.verification.token) }
  }

  @Test
  internal fun `create a new account when username and-or email are already in use returns an exception`() {
    val newAccount = AccountFixtures.newAccount()
    every { repository.saveAccount(newAccount) } returns IO { throw UsernameOrEmailNotAvailableException() }

    val result = service.createAccount(newAccount)

    assertTrue(result.isLeft())
  }

  @Test
  internal fun `verifying an account when is an starter account returns the verified account with default profile`() {
    val starterAccount = AccountFixtures.starterAccount()
    every { repository.findVerifiableAccount(AccountFixtures.username) } returns IO { starterAccount }
    every { repository.verifyAccount(starterAccount) } returns IO.unit

    val result = service.verifyAccount(AccountFixtures.username, AccountFixtures.verificationToken)

    assertTrue(result.isRight())
    verify { repository.verifyAccount(starterAccount) }
  }

  @Test
  internal fun `verifying an account with an invalid token returns an exception`() {
    every { repository.findVerifiableAccount(AccountFixtures.username) } returns IO { starterAccount() }

    val result = service.verifyAccount(AccountFixtures.username, "invalid-token")

    assertTrue(result.isLeft())
  }

  @Test
  internal fun `verifying an account that does not exist returns an error`() {
    every { repository.findVerifiableAccount(AccountFixtures.username) } returns IO { throw AccountNotFoundException() }

    val result = service.verifyAccount(AccountFixtures.username, AccountFixtures.verificationToken)

    assertTrue(result.isLeft())
  }

  @Test
  internal fun `delete an account that exists succeeds`() {
    every { repository.deleteAccount(AccountFixtures.username) } returns IO.unit

    val result = service.deleteAccount(AccountFixtures.username)

    assertTrue(result.isRight())
  }

  @Test
  internal fun `delete a non existing account returns an error`() {
    every { repository.deleteAccount(AccountFixtures.username) } returns IO { throw AccountNotFoundException() }

    val result = service.deleteAccount(AccountFixtures.username)

    assertTrue(result.isLeft())
  }

}