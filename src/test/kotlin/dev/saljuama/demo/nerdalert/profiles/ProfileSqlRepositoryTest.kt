package dev.saljuama.demo.nerdalert.profiles

import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.newAccount
import dev.saljuama.demo.nerdalert.profiles.ProfilesFixtures.profile
import dev.saljuama.demo.nerdalert.testutils.DbTestUtils
import dev.saljuama.demos.nerdalert.Tables
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
internal class ProfileSqlRepositoryTest {

  @Autowired private lateinit var sql: DSLContext
  @Autowired private lateinit var repository: ProfileSqlRepository

  @AfterEach
  internal fun tearDown() {
    DbTestUtils.wipeTables(sql, listOf(
      Tables.ACCOUNT,
      Tables.USER_PROFILE
    ))
  }

  @Test
  internal fun `when account exists and profile is not initialized throw ProfileNotInitialized`() {
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = "User1"))

    assertThrows(ProfileNotInitializedException::class.java) {
      repository.findProfile("User1").unsafeRunSync()
    }
  }

  @Test
  internal fun `when account does not exist throw ProfileNotFound`() {
    assertThrows(ProfileNotFoundException::class.java) {
      repository.findProfile("non-existing-user").unsafeRunSync()
    }
  }

  @Test
  internal fun `when account exists and profile is initialized return the profile`() {
    val username = "User1"
    val initializedProfile = profile().copy(username = username)
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = username))
    DbTestUtils.createProfileForUser(sql, initializedProfile)

    val profile = repository.findProfile(username).unsafeRunSync()

    assertEquals(initializedProfile, profile)
  }

}
