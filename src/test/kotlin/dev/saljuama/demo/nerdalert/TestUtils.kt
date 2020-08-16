package dev.saljuama.demo.nerdalert

import dev.saljuama.demo.nerdalert.accounts.*
import dev.saljuama.demo.nerdalert.profiles.Profile
import dev.saljuama.demo.nerdalert.profiles.ProfileFixtures
import dev.saljuama.demos.nerdalert.Tables.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.jooq.*
import java.time.LocalDate
import java.util.*
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

object DbTestUtils {

  fun wipeDatabase(sql: DSLContext) {
    sql.deleteFrom(ACCOUNT).execute()
    sql.deleteFrom(ACCOUNT_VERIFICATION).execute()
    sql.deleteFrom(USER_PROFILE).execute()
  }

  fun wipeTables(sql: DSLContext, tables: List<Table<out Record>>) {
    tables.forEach { sql.deleteFrom(it).execute() }
  }

  fun createStarterAccount(sql: DSLContext, account: StarterAccount = AccountFixtures.starterAccount()) {
    val accountRecord = sql.newRecord(ACCOUNT)
      .setUsername(account.username)
      .setEmail(account.email)
      .setPassword(AccountFixtures.password)
      .setRegistered(account.registered)
      .setVerified(false)
    sql.insertInto(ACCOUNT).set(accountRecord).execute()
    val verificationRecord = sql.newRecord(ACCOUNT_VERIFICATION)
      .setUsername(account.username)
      .setToken(account.verification.token)
      .setIssuedAt(LocalDate.now())
    sql.insertInto(ACCOUNT_VERIFICATION).set(verificationRecord).execute()
  }

  fun createVerifiedAccount(sql: DSLContext, account: NewAccount = AccountFixtures.newAccount()) {
    val record = sql.newRecord(ACCOUNT)
      .setUsername(account.username)
      .setEmail(account.email)
      .setPassword(account.password)
      .setRegistered(LocalDate.now())
      .setVerified(true)
    sql.insertInto(ACCOUNT).set(record).execute()
  }

  fun createProfileForUser(sql: DSLContext, profile: Profile = ProfileFixtures.profile()) {
    val record = sql.newRecord(USER_PROFILE)
      .setUsername(profile.username)
      .setFirstName(profile.firstName)
      .setLastName(profile.lastName)
      .setDescription(profile.description)
      .setImageUrl(profile.avatar)
    sql.insertInto(USER_PROFILE).set(record).execute()
  }

}

object JwtTestUtils {
  // same values as `jwt` properties in `integration-test` profile
  private const val secret: String = "this is a very random secret that needs to be 256 bits long at least, so not sure if this is going to be long enough but we will see soon"
  private const val tokenTimeoutInSeconds: Int = 108000

  fun generateAuthToken(username: String): String {
    val nowInMillis = System.currentTimeMillis()
    return Jwts.builder()
      .setSubject(username)
      .setIssuedAt(Date(nowInMillis))
      .setExpiration(Date(nowInMillis + (tokenTimeoutInSeconds * 1000)))
      .signWith(SecretKeySpec(DatatypeConverter.parseBase64Binary(secret), SignatureAlgorithm.HS256.jcaName))
      .compact()
  }
}
