package dev.saljuama.demo.nerdalert.accounts

import arrow.core.getOrElse
import dev.saljuama.demo.nerdalert.testutils.DbTestUtils
import org.jooq.DSLContext
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("integration-test")
internal class AccountsRepositoryIntegrationTest(
  @Autowired val sql: DSLContext
) {

  lateinit var accountsRepository: AccountsRepository

  @BeforeEach
  internal fun setUp() {
    accountsRepository = AccountsRepository(sql)
  }

  @AfterEach
  internal fun tearDown() {
    DbTestUtils.cleanupDatabase(sql)
  }

  @Test
  internal fun `creating a new account when username is available, persists account and a generated token in DB`() {
    val savedAccount = accountsRepository.createNewAccount(newAccount())
      .getOrElse { null }!!

    assertNotNull(savedAccount.verification?.token)
  }

  @Test
  internal fun `creating a new account when username is in use, does not persist anything in DB`() {
    accountsRepository.createNewAccount(newAccount())
    val result = accountsRepository.createNewAccount(newAccount())

    assertTrue(result.isLeft())
  }

  @Test
  internal fun `verifying an account with correct username and token combination, updates account and remove verification from DB`() {
    val token = accountsRepository.createNewAccount(newAccount())
      .map { account -> account.verification?.token!! }
      .getOrElse { null }!!

    val verifiedAccount = accountsRepository.verifyNewAccount("user1", token)
      .getOrElse { null }!!

    assertTrue(verifiedAccount.verified)
  }

  @Test
  internal fun `verifying an account with incorrect username and token combination, does nothing`() {
    accountsRepository.createNewAccount(newAccount())

    val result = accountsRepository.verifyNewAccount("user1", "fakeToken")

    assertTrue(result.isLeft())
  }

  private fun newAccount() = NewAccount("user1", "user1@email.com", "superSecret")
}
