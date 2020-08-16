package dev.saljuama.demo.nerdalert.profiles

import arrow.fx.IO
import dev.saljuama.demo.nerdalert.Tables.ACCOUNT
import dev.saljuama.demo.nerdalert.Tables.USER_PROFILE
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

  override fun upsertProfile(profile: Profile): IO<Unit> {
    return IO {
      sql.selectFrom(ACCOUNT).where(ACCOUNT.USERNAME.eq(profile.username)).fetchOne()
        ?: throw ProfileNotFoundException()

      val record = sql.newRecord(USER_PROFILE)
        .setUsername(profile.username)
        .setFirstName(profile.firstName)
        .setLastName(profile.lastName)
        .setDescription(profile.description)
        .setImageUrl(profile.avatar)
      sql.insertInto(USER_PROFILE).set(record)
        .onDuplicateKeyUpdate().set(record)
        .execute()
      Unit
    }
  }

  override fun findVerifiedProfiles(): IO<List<Profile>> {
    return IO {
      sql.selectFrom(USER_PROFILE
        .innerJoin(ACCOUNT).on(ACCOUNT.USERNAME.eq(USER_PROFILE.USERNAME)))
        .where(ACCOUNT.VERIFIED.eq(true))
        .fetch { record ->
          Profile(
            record.getValue(USER_PROFILE.USERNAME),
            record.getValue(USER_PROFILE.FIRST_NAME),
            record.getValue(USER_PROFILE.LAST_NAME),
            record.getValue(USER_PROFILE.DESCRIPTION),
            record.getValue(USER_PROFILE.IMAGE_URL)
          )
        }
    }
  }

}
