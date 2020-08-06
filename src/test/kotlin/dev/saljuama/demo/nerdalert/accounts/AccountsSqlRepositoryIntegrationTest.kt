package dev.saljuama.demo.nerdalert.accounts

import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.accountWithProfile
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.newAccount
import dev.saljuama.demo.nerdalert.accounts.registration.AccountRegistrationRepository
import dev.saljuama.demo.nerdalert.accounts.registration.NewAccount
import dev.saljuama.demo.nerdalert.testutils.DbTestUtils
import dev.saljuama.demos.nerdalert.Tables.*
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("integration-test")
internal class AccountsSqlRepositoryIntegrationTest(
  @Autowired val sql: DSLContext,
  @Autowired val repository: AccountsSqlRepository,
  @Autowired val registrationRepository: AccountRegistrationRepository
) {

  @AfterEach
  fun tearDown() {
    DbTestUtils.wipeTables(sql, listOf(
      ACCOUNT,
      ACCOUNT_VERIFICATION
    ))
  }

  @Test
  internal fun `find verified account that exists returns the account`() {
    val newAccount = newAccount()
    persistVerifiedAccount(newAccount)

    val result = repository.findVerifiedAccount(newAccount.username).unsafeRunSync()

    assertEquals(newAccount.username, result.username)
  }

  @Test
  internal fun `find verified account with profile also returns the profile`() {
    val newAccount = newAccount()
    persistVerifiedAccount(newAccount)
    val accountWithProfile = accountWithProfile()
    repository.updateProfile(accountWithProfile).unsafeRunSync()

    val result = repository.findVerifiedAccount(newAccount.username).unsafeRunSync()

    assertEquals(newAccount.username, result.username)
    assertEquals(accountWithProfile.profile?.firstName, result.profile?.firstName)
  }

  @Test
  internal fun `find verified account that does not exist throws exception`() {
    assertThrows(AccountNotFoundException::class.java) {
      repository.findVerifiedAccount("username-that-does-not-exist").unsafeRunSync()
    }
  }

  @Test
  internal fun `find verified account for an account that exist but it is not verified throws exception`() {
    val newAccount = newAccount()
    persistNonVerifiedAccount(newAccount)

    assertThrows(AccountNotFoundException::class.java) {
      repository.findVerifiedAccount(newAccount.username).unsafeRunSync()
    }
  }

  @Test
  internal fun `updating user profile creates one if it did not exist`() {
    val newAccount = newAccount()
    persistVerifiedAccount(newAccount)

    assertEquals(0, sql.fetchCount(USER_PROFILE))
    repository.updateProfile(accountWithProfile()).unsafeRunSync()
    assertEquals(1, sql.fetchCount(USER_PROFILE))
  }

  @Test
  internal fun `updating user profile updates the values if it already existed`() {
    val newAccount = newAccount()
    persistVerifiedAccount(newAccount)
    repository.updateProfile(accountWithProfile()).unsafeRunSync()

    val updatedProfile = accountWithProfile().copy(profile = UserProfile("AnotherFirstName", "AnotherLastName", "Another Description"))
    repository.updateProfile(updatedProfile).unsafeRunSync()

    assertEquals(1, sql.fetchCount(USER_PROFILE))
  }

  @Test
  internal fun `deleting a non verified account also deletes the verification on cascade`() {
    val newAccount = newAccount()
    persistNonVerifiedAccount(newAccount)

    repository.deleteAccount(newAccount.username).unsafeRunSync()

    assertEquals(0, sql.fetchCount(ACCOUNT))
    assertEquals(0, sql.fetchCount(ACCOUNT_VERIFICATION))
  }

  @Test
  internal fun `deleting a verified account also deletes on cascade the user profile`() {
    val newAccount = newAccount()
    persistVerifiedAccount(newAccount)
    repository.updateProfile(accountWithProfile()).unsafeRunSync()

    repository.deleteAccount(newAccount.username).unsafeRunSync()

    assertEquals(0, sql.fetchCount(ACCOUNT))
    assertEquals(0, sql.fetchCount(USER_PROFILE))
  }

  @Test
  internal fun `deleting an account that does not exist throws an exception`() {
    assertThrows(AccountNotFoundException::class.java) {
      repository.deleteAccount("non-existing-user").unsafeRunSync()
    }
  }

  private fun persistVerifiedAccount(newAccount: NewAccount) {
    registrationRepository.saveAccount(newAccount).flatMap { registrationRepository.verifyAccount(it) }.unsafeRunSync()
  }

  private fun persistNonVerifiedAccount(newAccount: NewAccount) {
    registrationRepository.saveAccount(newAccount).unsafeRunSync()
  }

}
