package dev.saljuama.demo.nerdalert.accounts.registration

import arrow.fx.IO
import dev.saljuama.demo.nerdalert.accounts.AccountNotFoundException
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class AccountRegistrationTransactionalServiceTest {

  @MockK private lateinit var repository: AccountRegistrationRepository
  private lateinit var service: AccountRegistrationService

  @BeforeEach
  internal fun setUp() {
    service = AccountRegistrationTransactionalService(repository)
  }

  @Test
  internal fun `create a new account returns a starter account with a verification token`() {
    val newAccount = AccountsFixtures.newAccount()
    every { repository.saveAccount(newAccount) } returns IO { AccountsFixtures.starterAccount() }

    val result = service.createAccount(newAccount)

    assertTrue(result.isRight())
    result.map { starterAccount -> assertNotNull(starterAccount.verification.token) }
  }

  @Test
  internal fun `create a new account when username and-or email are already in use returns an exception`() {
    val newAccount = AccountsFixtures.newAccount()
    every { repository.saveAccount(newAccount) } returns IO { throw UsernameOrEmailNotAvailableException() }

    val result = service.createAccount(newAccount)

    assertTrue(result.isLeft())
  }

  @Test
  internal fun `verifying an account when is an starter account returns the verified account with default profile`() {
    val starterAccount = AccountsFixtures.starterAccount()
    every { repository.findVerifiableAccount(AccountsFixtures.username) } returns IO { starterAccount }
    every { repository.verifyAccount(starterAccount) } returns IO.unit

    val result = service.verifyAccount(AccountsFixtures.username, AccountsFixtures.verificationToken)

    assertTrue(result.isRight())
    result.map { account -> assertEquals(account.username, account.profile.firstName) }
    verify { repository.verifyAccount(starterAccount) }
  }

  @Test
  internal fun `verifying an account with an invalid token returns an exception`() {
    every { repository.findVerifiableAccount(AccountsFixtures.username) } returns IO { AccountsFixtures.starterAccount() }

    val result = service.verifyAccount(AccountsFixtures.username, "invalid-token")

    assertTrue(result.isLeft())
  }

  @Test
  internal fun `verifying an account that does not exist returns an error`() {
    every { repository.findVerifiableAccount(AccountsFixtures.username) } returns IO { throw AccountNotFoundException() }

    val result = service.verifyAccount(AccountsFixtures.username, AccountsFixtures.verificationToken)

    assertTrue(result.isLeft())
  }

}