package dev.saljuama.demo.nerdalert.friendship

import arrow.fx.IO
import dev.saljuama.demo.nerdalert.Tables.FRIENDSHIP
import dev.saljuama.demo.nerdalert.Tables.USER_PROFILE
import dev.saljuama.demo.nerdalert.enums.FriendshipStatus
import dev.saljuama.demo.nerdalert.profiles.ProfileNotFoundException
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class FriendshipSqlRepository(
  private val sql: DSLContext
) : FriendshipRepository {

  override fun createFriendshipRequest(toUsername: String, fromUsername: String): IO<Unit> {
    return IO {
      validateProfilesExist(toUsername, fromUsername)
      checkExistingFriendshipsOrRequests(fromUsername, toUsername)

      val friendshipRecord = sql.newRecord(FRIENDSHIP)
        .setFromUsername(fromUsername)
        .setToUsername(toUsername)
        .setStatus(FriendshipStatus.REQUESTED)

      sql.insertInto(FRIENDSHIP).set(friendshipRecord).execute()
    }

  }

  private fun checkExistingFriendshipsOrRequests(fromUsername: String, toUsername: String) {
    val request = sql
      .selectFrom(FRIENDSHIP)
      .where(FRIENDSHIP.FROM_USERNAME.eq(fromUsername))
      .and(FRIENDSHIP.TO_USERNAME.eq(toUsername))
      .fetchOne()
    when (request?.status) {
      FriendshipStatus.REQUESTED -> throw FriendshipRequestAlreadySentException()
      FriendshipStatus.ACCEPTED -> throw UsersAreFriendsAlreadyException()
      FriendshipStatus.REJECTED -> throw UsersAreFriendsAlreadyException()
    }
  }

  private fun validateProfilesExist(toUsername: String, fromUsername: String) {
    sql.selectFrom(USER_PROFILE).where(USER_PROFILE.USERNAME.eq(toUsername)).fetchOne()
      ?: throw ProfileNotFoundException()
    sql.selectFrom(USER_PROFILE).where(USER_PROFILE.USERNAME.eq(fromUsername)).fetchOne()
      ?: throw ProfileNotFoundException()
  }

}
