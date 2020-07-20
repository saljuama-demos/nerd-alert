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
internal class AccountsRepositoryTest(
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
  fun createNewAccount_usernameIsAvailable_createsTheNewAccount() {
    val newAccount = AccountEntity(null, "user1", "user1@email.com", "superSecret")

    val savedAccount = accountsRepository.createNewAccount(newAccount).getOrElse { null }!!

    assertNotNull(savedAccount.id)
    assertNotNull(savedAccount.verification)
  }

  @Test
  fun createNewAccount_usernameIsInUse_doesNotCreateAnAccount() {
    val newAccount = AccountEntity(null, "user1", "user1@email.com", "superSecret")

    accountsRepository.createNewAccount(newAccount)
    val result = accountsRepository.createNewAccount(newAccount)

    assertTrue(result.isLeft())
  }

  @Test
  fun verifyNewAccount_withCorrectCombination_verifiesTheAccount() {
    val newAccount = AccountEntity(null, "user1", "user1@email.com", "superSecret")
    val token = accountsRepository.createNewAccount(newAccount)
      .map { account -> account.verification?.token!! }
      .getOrElse { null }!!

    val verifiedAccount = accountsRepository.verifyNewAccount("user1", token)
      .getOrElse { null }!!

    assertTrue(verifiedAccount.verified)
  }

  @Test
  fun verifyNewAccount_withInvalidCombination_doesNothing() {
    val newAccount = AccountEntity(null, "user1", "user1@email.com", "superSecret")
    accountsRepository.createNewAccount(newAccount)

    val result = accountsRepository.verifyNewAccount("user1", "fakeToken")

    assertTrue(result.isLeft())
  }
}
