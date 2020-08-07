package dev.saljuama.demo.nerdalert.testutils

import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures
import dev.saljuama.demo.nerdalert.accounts.registration.NewAccount
import dev.saljuama.demo.nerdalert.accounts.registration.StarterAccount
import dev.saljuama.demo.nerdalert.profiles.Profile
import dev.saljuama.demo.nerdalert.profiles.ProfilesFixtures
import dev.saljuama.demos.nerdalert.Tables.*
import org.jooq.*
import java.time.LocalDate

object DbTestUtils {

  fun wipeDatabase(sql: DSLContext) {
    sql.deleteFrom(ACCOUNT).execute()
    sql.deleteFrom(ACCOUNT_VERIFICATION).execute()
    sql.deleteFrom(USER_PROFILE).execute()
  }

  fun wipeTables(sql: DSLContext, tables: List<Table<out Record>>) {
    tables.forEach { sql.deleteFrom(it).execute() }
  }

  fun createStarterAccount(sql: DSLContext, account: StarterAccount = AccountsFixtures.starterAccount()) {
    val accountRecord = sql.newRecord(ACCOUNT)
      .setUsername(account.username)
      .setEmail(account.email)
      .setPassword(AccountsFixtures.password)
      .setRegistered(account.registered)
      .setVerified(false)
    sql.insertInto(ACCOUNT).set(accountRecord).execute()
    val verificationRecord = sql.newRecord(ACCOUNT_VERIFICATION)
      .setUsername(account.username)
      .setToken(account.verification.token)
      .setIssuedAt(LocalDate.now())
    sql.insertInto(ACCOUNT_VERIFICATION).set(verificationRecord).execute()
  }

  fun createVerifiedAccount(sql: DSLContext, account: NewAccount = AccountsFixtures.newAccount()) {
    val record = sql.newRecord(ACCOUNT)
      .setUsername(account.username)
      .setEmail(account.email)
      .setPassword(account.password)
      .setRegistered(LocalDate.now())
      .setVerified(true)
    sql.insertInto(ACCOUNT).set(record).execute()
  }

  fun createProfileForUser(sql: DSLContext, profile: Profile = ProfilesFixtures.profile()) {
    val record = sql.newRecord(USER_PROFILE)
      .setUsername(profile.username)
      .setFirstName(profile.firstName)
      .setLastName(profile.lastName)
      .setDescription(profile.description)
      .setImageUrl(profile.avatar)
    sql.insertInto(USER_PROFILE).set(record).execute()
  }

}
