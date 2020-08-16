package dev.saljuama.demo.nerdalert.accounts

import dev.saljuama.demo.nerdalert.DbTestUtils
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT_VERIFICATION
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("integration-test")
internal class AccountSqlRepositoryIntegrationTest {

  @Autowired private lateinit var sql: DSLContext
  @Autowired private lateinit var repository: AccountRepository

  @AfterEach
  fun tearDown() {
    DbTestUtils.wipeTables(sql, listOf(
      ACCOUNT,
      ACCOUNT_VERIFICATION
    ))
  }

  @Test
  internal fun `saving a new account creates the account and a verification`() {
    val newAccount = AccountFixtures.newAccount()

    val result = repository.saveAccount(newAccount).unsafeRunSync()

    assertNotNull(result.verification.token)
    assertEquals(1, sql.fetchCount(ACCOUNT))
    assertEquals(1, sql.fetchCount(ACCOUNT_VERIFICATION))
  }

  @Test
  internal fun `saving a new account with an username in use will fail`() {
    val newAccount = AccountFixtures.newAccount()
    repository.saveAccount(newAccount).unsafeRunSync()

    val result = repository.saveAccount(newAccount).attempt().unsafeRunSync()

    assertTrue(result.isLeft())
    assertEquals(1, sql.fetchCount(ACCOUNT))
    assertEquals(1, sql.fetchCount(ACCOUNT_VERIFICATION))
  }

  @Test
  internal fun `find verifiable accounts finds a starter account that exists`() {
    val newAccount = AccountFixtures.newAccount()
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
    val newAccount = AccountFixtures.newAccount()
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
  internal fun `deleting a non verified account also deletes the verification on cascade`() {
    DbTestUtils.createStarterAccount(sql, AccountFixtures.starterAccount().copy(username = "Pepe"))

    repository.deleteAccount("Pepe").unsafeRunSync()

    assertEquals(0, sql.fetchCount(ACCOUNT))
    assertEquals(0, sql.fetchCount(ACCOUNT_VERIFICATION))
  }

  @Test
  internal fun `deleting an account that does not exist throws an exception`() {
    assertThrows(AccountNotFoundException::class.java) {
      repository.deleteAccount("non-existing-user").unsafeRunSync()
    }
  }

}