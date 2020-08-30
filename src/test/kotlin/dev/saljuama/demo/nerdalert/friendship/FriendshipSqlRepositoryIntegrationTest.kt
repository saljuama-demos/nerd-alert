package dev.saljuama.demo.nerdalert.friendship

import dev.saljuama.demo.nerdalert.DbTestUtils
import dev.saljuama.demo.nerdalert.Tables.*
import dev.saljuama.demo.nerdalert.accounts.AccountFixtures.newAccount
import dev.saljuama.demo.nerdalert.enums.FriendshipStatus
import dev.saljuama.demo.nerdalert.profiles.ProfileFixtures.profile
import dev.saljuama.demo.nerdalert.profiles.ProfileNotFoundException
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
internal class FriendshipSqlRepositoryIntegrationTest {

  @Autowired private lateinit var sql: DSLContext
  @Autowired private lateinit var repository: FriendshipRepository

  @AfterEach
  fun tearDown() {
    DbTestUtils.wipeTables(sql, listOf(
      ACCOUNT,
      USER_PROFILE,
      FRIENDSHIP
    ))
  }

  @Test
  internal fun `creating a new friendship request for an existing account returns succeeds`() {
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = "Pepe", email = "pepe"))
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = "Angelika", email = "angi"))
    DbTestUtils.createProfileForUser(sql, profile().copy(username = "Pepe"))
    DbTestUtils.createProfileForUser(sql, profile().copy(username = "Angelika"))

    repository.createFriendshipRequest("Pepe", "Angelika").unsafeRunSync()

    val friendshipRequestsForPepe = sql.selectCount()
      .from(FRIENDSHIP)
      .where(FRIENDSHIP.TO_USERNAME.eq("Pepe"))
      .and(FRIENDSHIP.STATUS.eq(FriendshipStatus.REQUESTED))
      .execute()
    assertEquals(1, friendshipRequestsForPepe)
  }

  @Test
  internal fun `creating a new friendshipw request for non existing accounts fails`() {
    assertThrows(ProfileNotFoundException::class.java) {
      repository.createFriendshipRequest("Pepe", "Angelika").unsafeRunSync()
    }
  }

  @Test
  internal fun `creating a new friendship request when there is a pending request fails`() {
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = "Pepe", email = "pepe"))
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = "Angelika", email = "angi"))
    DbTestUtils.createProfileForUser(sql, profile().copy(username = "Pepe"))
    DbTestUtils.createProfileForUser(sql, profile().copy(username = "Angelika"))

    repository.createFriendshipRequest("Pepe", "Angelika").unsafeRunSync()

    assertThrows(FriendshipRequestAlreadySentException::class.java) {
      repository.createFriendshipRequest("Pepe", "Angelika").unsafeRunSync()
    }
  }


}