package dev.saljuama.demo.nerdalert.accounts

import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.newAccount
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.starterAccount
import dev.saljuama.demo.nerdalert.profiles.ProfilesFixtures.profile
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
internal class AccountsSqlRepositoryIntegrationTest {

  @Autowired private lateinit var sql: DSLContext
  @Autowired private lateinit var repository: AccountsSqlRepository

  @AfterEach
  fun tearDown() {
    DbTestUtils.wipeTables(sql, listOf(
      ACCOUNT,
      ACCOUNT_VERIFICATION,
      USER_PROFILE
    ))
  }

  @Test
  internal fun `deleting a non verified account also deletes the verification on cascade`() {
    DbTestUtils.createStarterAccount(sql, starterAccount().copy(username = "Pepe"))

    repository.deleteAccount("Pepe").unsafeRunSync()

    assertEquals(0, sql.fetchCount(ACCOUNT))
    assertEquals(0, sql.fetchCount(ACCOUNT_VERIFICATION))
  }

  @Test
  internal fun `deleting a verified account also deletes on cascade the user profile`() {
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = "Pepe"))
    DbTestUtils.createProfileForUser(sql, profile().copy(username = "Pepe"))

    repository.deleteAccount("Pepe").unsafeRunSync()

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
