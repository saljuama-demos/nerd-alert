package dev.saljuama.demo.nerdalert.accounts

import arrow.fx.IO
import dev.saljuama.demos.nerdalert.Tables.USER_PROFILE
import dev.saljuama.demos.nerdalert.tables.Account.ACCOUNT
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Component

@Component
class AccountsSqlRepository(
  private val sql: DSLContext
) : AccountsRepository {

  override fun findVerifiedAccount(username: String): IO<Account> {
    return IO {
      val result = sql
        .select()
        .from(ACCOUNT.leftJoin(USER_PROFILE).on(USER_PROFILE.USERNAME.eq(ACCOUNT.USERNAME)))
        .where(ACCOUNT.USERNAME.eq(username))
        .and(ACCOUNT.VERIFIED.eq(true))
        .fetchOne()
        ?: throw AccountNotFoundException()

      Account(
        result.getValue(ACCOUNT.USERNAME),
        result.getValue(ACCOUNT.EMAIL),
        result.getValue(ACCOUNT.REGISTERED),
        parseUserProfile(result)
      )
    }
  }

  private fun parseUserProfile(result: Record): UserProfile? = when {
    result.getValue(USER_PROFILE.FIRST_NAME) != null -> {
      UserProfile(
        result.getValue(USER_PROFILE.FIRST_NAME),
        result.getValue(USER_PROFILE.LAST_NAME),
        result.getValue(USER_PROFILE.DESCRIPTION),
        result.getValue(USER_PROFILE.IMAGE_URL)
      )
    }
    else -> null
  }

  override fun updateProfile(account: Account): IO<Unit> {
    return IO {
      val record = sql.newRecord(USER_PROFILE)
      record.username = account.username
      record.firstName = account.profile?.firstName
      record.lastName = account.profile?.lastName
      record.description = account.profile?.description
      record.imageUrl = account.profile?.imageUrl
      sql
        .insertInto(USER_PROFILE)
        .set(record)
        .onDuplicateKeyUpdate()
        .set(record)
        .execute()
      Unit
    }
  }

  override fun deleteAccount(username: String): IO<Unit> {
    return IO {
      val deletedRows = sql.deleteFrom(ACCOUNT)
        .where(ACCOUNT.USERNAME.eq(username))
        .execute()
      if (deletedRows < 1)
        throw AccountNotFoundException()
    }
  }

}
