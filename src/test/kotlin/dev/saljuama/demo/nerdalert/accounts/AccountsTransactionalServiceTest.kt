package dev.saljuama.demo.nerdalert.accounts

import arrow.fx.IO
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.account
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.userProfile
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.username
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
  internal fun `updating a profile for an existing account returns the account with the updated profile`() {
    every { repository.findVerifiedAccount(username) } returns IO { account() }
    every { repository.updateProfile(any()) } returns IO.unit

    val newProfile = userProfile()
    val result = service.updateProfile(username, newProfile)

    assertTrue(result.isRight())
    result.map { account -> assertEquals(newProfile, account.profile) }
  }

  @Test
  internal fun `updating a profile for a non-existing or non-validated account returns an error`() {
    every { repository.findVerifiedAccount(username) } returns IO { throw AccountNotFoundException() }

    val result = service.updateProfile(username, userProfile())

    assertTrue(result.isLeft())
  }

  @Test
  internal fun `delete an account that exists succeeds`() {
    every { repository.deleteAccount(username) } returns IO.unit

    val result = service.deleteAccount(username)

    assertTrue(result.isRight())
  }

  @Test
  internal fun `delete a non existing account returns an error`() {
    every { repository.deleteAccount(username) } returns IO { throw AccountNotFoundException() }

    val result = service.deleteAccount(username)

    assertTrue(result.isLeft())
  }

}