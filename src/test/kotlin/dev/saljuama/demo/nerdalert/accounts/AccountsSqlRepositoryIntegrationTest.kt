package dev.saljuama.demo.nerdalert.accounts

import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.accountWithProfile
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.newAccount
import dev.saljuama.demo.nerdalert.testutils.DbTestUtils
import dev.saljuama.demos.nerdalert.Tables.*
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("integration-test")
internal class AccountsSqlRepositoryIntegrationTest(
  @Autowired val sql: DSLContext,
  @Autowired val repository: AccountsSqlRepository
) {

  @AfterEach
  fun tearDown() {
    DbTestUtils.wipeTables(sql, listOf(
      ACCOUNT,
      ACCOUNT_VERIFICATION
    ))
  }

  @Test
  internal fun `saving a new account creates the account and a verification`() {
    val newAccount = newAccount()

    val result = repository.saveAccount(newAccount).unsafeRunSync()

    assertNotNull(result.verification.token)
    assertEquals(1, sql.fetchCount(ACCOUNT))
    assertEquals(1, sql.fetchCount(ACCOUNT_VERIFICATION))
  }

  @Test
  internal fun `saving a new account with an username in use will fail`() {
    val newAccount = newAccount()
    repository.saveAccount(newAccount).unsafeRunSync()

    val result = repository.saveAccount(newAccount).attempt().unsafeRunSync()

    assertTrue(result.isLeft())
    assertEquals(1, sql.fetchCount(ACCOUNT))
    assertEquals(1, sql.fetchCount(ACCOUNT_VERIFICATION))
  }

  @Test
  internal fun `find verifiable accounts finds a starter account that exists`() {
    val newAccount = newAccount()
    repository.saveAccount(newAccount).unsafeRunSync()

    val result = repository.findVerifiableAccount(newAccount.username).unsafeRunSync()

    assertEquals(newAccount.username, result.username)
  }

  @Test
  internal fun `find verifiable accounts for an account that does not exist throws exception`() {
    assertThrows(AccountNotFoundException::class.java) {
      repository.findVerifiableAccount("non-existing-username").unsafeRunSync()
    }
  }

  @Test
  internal fun `verifying an account updates the verified field and deletes a verification record`() {
    val newAccount = newAccount()
    val starterAccount = repository.saveAccount(newAccount).unsafeRunSync()

    repository.verifyAccount(starterAccount).unsafeRunSync()

    assertTrue(sql
      .selectFrom(ACCOUNT)
      .where(ACCOUNT.USERNAME.eq(newAccount.username))
      .fetchOne()
      .getValue(ACCOUNT.VERIFIED)
    )
    assertEquals(0, sql.fetchCount(ACCOUNT_VERIFICATION))
  }

  @Test
  internal fun `find verified account that exists returns the account`() {
    val newAccount = newAccount()
    repository.saveAccount(newAccount).flatMap { repository.verifyAccount(it) }.unsafeRunSync()

    val result = repository.findVerifiedAccount(newAccount.username).unsafeRunSync()

    assertEquals(newAccount.username, result.username)
  }

  @Test
  internal fun `find verified account with profile also returns the profile`() {
    val newAccount = newAccount()
    repository.saveAccount(newAccount).flatMap { repository.verifyAccount(it) }.unsafeRunSync()
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
    repository.saveAccount(newAccount).unsafeRunSync()

    assertThrows(AccountNotFoundException::class.java) {
      repository.findVerifiedAccount(newAccount.username).unsafeRunSync()
    }
  }

  @Test
  internal fun `updating user profile creates one if it did not exist`() {
    val newAccount = newAccount()
    repository.saveAccount(newAccount).flatMap { repository.verifyAccount(it) }.unsafeRunSync()


    assertEquals(0, sql.fetchCount(USER_PROFILE))
    repository.updateProfile(accountWithProfile()).unsafeRunSync()
    assertEquals(1, sql.fetchCount(USER_PROFILE))
  }

  @Test
  internal fun `updating user profile updates the values if it already existed`() {
    val newAccount = newAccount()
    repository.saveAccount(newAccount).flatMap { repository.verifyAccount(it) }.unsafeRunSync()
    repository.updateProfile(accountWithProfile()).unsafeRunSync()

    val updatedProfile = accountWithProfile().copy(profile = UserProfile("AnotherFirstName", "AnotherLastName", "Another Description"))
    repository.updateProfile(updatedProfile).unsafeRunSync()

    assertEquals(1, sql.fetchCount(USER_PROFILE))
  }

  @Test
  internal fun `deleting a non verified account also deletes the verification on cascade`() {
    val newAccount = newAccount()
    repository.saveAccount(newAccount).unsafeRunSync()

    repository.deleteAccount(newAccount.username).unsafeRunSync()

    assertEquals(0, sql.fetchCount(ACCOUNT))
    assertEquals(0, sql.fetchCount(ACCOUNT_VERIFICATION))
  }

  @Test
  internal fun `deleting a verified account also deletes on cascade the user profile`() {
    val newAccount = newAccount()
    repository.saveAccount(newAccount).flatMap { repository.verifyAccount(it) }.unsafeRunSync()
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

}