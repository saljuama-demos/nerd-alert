package dev.saljuama.demo.nerdalert.profiles

import dev.saljuama.demo.nerdalert.DbTestUtils
import dev.saljuama.demo.nerdalert.accounts.AccountFixtures.newAccount
import dev.saljuama.demo.nerdalert.accounts.NewAccount
import dev.saljuama.demo.nerdalert.accounts.StarterAccount
import dev.saljuama.demo.nerdalert.profiles.ProfileFixtures.profile
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT
import dev.saljuama.demos.nerdalert.Tables.USER_PROFILE
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
      ACCOUNT,
      USER_PROFILE
    ))
  }

  @Test
  internal fun `find a profile for an existing account with a profile returns the profile`() {
    val username = "User1"
    val initializedProfile = profile().copy(username = username)
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = username))
    DbTestUtils.createProfileForUser(sql, initializedProfile)

    val profile = repository.findProfile(username).unsafeRunSync()

    assertEquals(initializedProfile, profile)
  }

  @Test
  internal fun `find a profile for an account without initialised profile throws ProfileNotInitialized`() {
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = "User1"))

    assertThrows(ProfileNotInitializedException::class.java) {
      repository.findProfile("User1").unsafeRunSync()
    }
  }

  @Test
  internal fun `find a profile for a non existing account throw ProfileNotFound`() {
    assertThrows(ProfileNotFoundException::class.java) {
      repository.findProfile("non-existing-user").unsafeRunSync()
    }
  }

  @Test
  internal fun `upsert a profile for an account without a profile creates the profile`() {
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = "Pepe"))

    repository.upsertProfile(profile().copy(username = "Pepe")).unsafeRunSync()

    assertEquals(1, sql.fetchCount(USER_PROFILE, USER_PROFILE.USERNAME.eq("Pepe")))
  }

  @Test
  internal fun `upsert a profile for an account with a profile updates the profile`() {
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = "Pepe"))
    DbTestUtils.createProfileForUser(sql, profile().copy(username = "Pepe"))

    repository.upsertProfile(profile().copy(username = "Pepe", firstName = "Jose")).unsafeRunSync()

    assertEquals(0, sql.fetchCount(USER_PROFILE, USER_PROFILE.FIRST_NAME.eq(ProfileFixtures.firstName)))
    assertEquals(1, sql.fetchCount(USER_PROFILE, USER_PROFILE.FIRST_NAME.eq("Jose")))
  }

  @Test
  internal fun `upsert a profile for a non existing account throws ProfileNotFound`() {
    assertThrows(ProfileNotFoundException::class.java) {
      repository.upsertProfile(profile()).unsafeRunSync()
    }
  }

  @Test
  internal fun `finding verified profiles returns the list of verified profiles`() {
    DbTestUtils.createVerifiedAccount(sql, NewAccount("user1", "user1@email.com", "secret"))
    DbTestUtils.createVerifiedAccount(sql, NewAccount("user2", "user2@email.com", "secret"))
    DbTestUtils.createVerifiedAccount(sql, NewAccount("user3", "user3@email.com", "secret"))
    DbTestUtils.createProfileForUser(sql, profile().copy(username = "user1"))
    DbTestUtils.createProfileForUser(sql, profile().copy(username = "user2"))
    DbTestUtils.createProfileForUser(sql, profile().copy(username = "user3"))

    val verifiedProfiles = repository.findVerifiedProfiles().unsafeRunSync()

    assertEquals(3, verifiedProfiles.size)
  }

  @Test
  internal fun `finding verified profiles ignore starter accounts`() {
    DbTestUtils.createStarterAccount(sql, StarterAccount("user1", "email1"))
    DbTestUtils.createStarterAccount(sql, StarterAccount("user2", "email2"))
    DbTestUtils.createStarterAccount(sql, StarterAccount("user3", "email3"))

    val verifiedProfiles = repository.findVerifiedProfiles().unsafeRunSync()

    assertEquals(0, verifiedProfiles.size)
  }
}
