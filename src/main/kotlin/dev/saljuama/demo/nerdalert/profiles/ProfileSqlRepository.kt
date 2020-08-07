package dev.saljuama.demo.nerdalert.profiles

import arrow.fx.IO
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class ProfileSqlRepository(
  private val sql: DSLContext
) : ProfileRepository {

  override fun findProfile(username: String): IO<Profile> {
    TODO("not implemented")
  }

}
