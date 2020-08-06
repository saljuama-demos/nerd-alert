package dev.saljuama.demo.nerdalert.accounts.registration

import dev.saljuama.demo.nerdalert.accounts.AccountNotFoundException
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures
import dev.saljuama.demo.nerdalert.testutils.DbTestUtils
import dev.saljuama.demos.nerdalert.Tables
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("integration-test")
internal class AccountRegistrationSqlRepositoryIntegrationTest(
  @Autowired val sql: DSLContext,
  @Autowired val repository: AccountRegistrationSqlRepository
) {

  @AfterEach
  fun tearDown() {
    DbTestUtils.wipeTables(sql, listOf(
      Tables.ACCOUNT,
      Tables.ACCOUNT_VERIFICATION
    ))
  }

  @Test
  internal fun `saving a new account creates the account and a verification`() {
    val newAccount = AccountsFixtures.newAccount()

    val result = repository.saveAccount(newAccount).unsafeRunSync()

    assertNotNull(result.verification.token)
    assertEquals(1, sql.fetchCount(Tables.ACCOUNT))
    assertEquals(1, sql.fetchCount(Tables.ACCOUNT_VERIFICATION))
  }

  @Test
  internal fun `saving a new account with an username in use will fail`() {
    val newAccount = AccountsFixtures.newAccount()
    repository.saveAccount(newAccount).unsafeRunSync()

    val result = repository.saveAccount(newAccount).attempt().unsafeRunSync()

    assertTrue(result.isLeft())
    assertEquals(1, sql.fetchCount(Tables.ACCOUNT))
    assertEquals(1, sql.fetchCount(Tables.ACCOUNT_VERIFICATION))
  }

  @Test
  internal fun `find verifiable accounts finds a starter account that exists`() {
    val newAccount = AccountsFixtures.newAccount()
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
    val newAccount = AccountsFixtures.newAccount()
    val starterAccount = repository.saveAccount(newAccount).unsafeRunSync()

    repository.verifyAccount(starterAccount).unsafeRunSync()

    assertTrue(sql
      .selectFrom(Tables.ACCOUNT)
      .where(Tables.ACCOUNT.USERNAME.eq(newAccount.username))
      .fetchOne()
      .getValue(Tables.ACCOUNT.VERIFIED)
    )
    assertEquals(0, sql.fetchCount(Tables.ACCOUNT_VERIFICATION))
  }

}