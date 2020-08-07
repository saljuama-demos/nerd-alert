package dev.saljuama.demo.nerdalert.profiles

import arrow.fx.IO
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT
import dev.saljuama.demos.nerdalert.Tables.USER_PROFILE
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class ProfileSqlRepository(
  private val sql: DSLContext
) : ProfileRepository {

  override fun findProfile(username: String): IO<Profile> {
    return IO {
      sql.selectFrom(ACCOUNT).where(ACCOUNT.USERNAME.eq(username)).fetchOne()
        ?: throw ProfileNotFoundException()
      val result = sql.selectFrom(USER_PROFILE).where(USER_PROFILE.USERNAME.eq(username)).fetchOne()
        ?: throw ProfileNotInitializedException()
      Profile(
        result.getValue(USER_PROFILE.USERNAME),
        result.getValue(USER_PROFILE.FIRST_NAME),
        result.getValue(USER_PROFILE.LAST_NAME),
        result.getValue(USER_PROFILE.DESCRIPTION),
        result.getValue(USER_PROFILE.IMAGE_URL)
      )
    }
  }

}
