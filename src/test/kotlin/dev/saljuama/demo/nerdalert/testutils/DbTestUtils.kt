package dev.saljuama.demo.nerdalert.testutils

import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures
import dev.saljuama.demo.nerdalert.accounts.registration.NewAccount
import dev.saljuama.demo.nerdalert.profiles.Profile
import dev.saljuama.demos.nerdalert.Tables
import org.jooq.*
import java.time.LocalDate

object DbTestUtils {

  fun wipeDatabase(sql: DSLContext) {
    sql.deleteFrom(Tables.ACCOUNT).execute()
    sql.deleteFrom(Tables.ACCOUNT_VERIFICATION).execute()
    sql.deleteFrom(Tables.USER_PROFILE).execute()
  }

  fun wipeTables(sql: DSLContext, tables: List<Table<out Record>>) {
    tables.forEach { sql.deleteFrom(it).execute() }
  }

  fun createVerifiedAccount(sql: DSLContext, account: NewAccount = AccountsFixtures.newAccount()) {
    val record = sql.newRecord(Tables.ACCOUNT)
      .setUsername(account.username)
      .setEmail(account.email)
      .setPassword(account.password)
      .setRegistered(LocalDate.now())
      .setVerified(true)
    sql.insertInto(Tables.ACCOUNT).set(record).execute()
  }

  fun createProfileForUser(sql: DSLContext, profile: Profile) {
    val record = sql.newRecord(Tables.USER_PROFILE)
      .setUsername(profile.username)
      .setFirstName(profile.firstName)
      .setLastName(profile.lastName)
      .setDescription(profile.description)
      .setImageUrl(profile.avatar)
    sql.insertInto(Tables.USER_PROFILE).set(record).execute()
  }

}
