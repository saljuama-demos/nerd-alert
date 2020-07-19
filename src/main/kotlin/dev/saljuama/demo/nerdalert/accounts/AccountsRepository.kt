package dev.saljuama.demo.nerdalert.accounts

import dev.saljuama.demos.nerdalert.Tables.ACCOUNT
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT_VERIFICATION
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Result
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

data class AccountEntity(
  val id: Int?,
  val username: String,
  val email: String,
  val password: String,
  val registered: LocalDate = LocalDate.now(),
  val verification: AccountVerification? = null
)

data class AccountVerification(
  val token: String
)

@Component
class AccountsRepository(
  val sql: DSLContext
) {

  @Transactional
  fun createNewAccount(account: AccountEntity): AccountEntity {
    val savedAccount = persistNewAccount(account)
    return persistNewAccountVerification(savedAccount)
  }

  private fun persistNewAccount(account: AccountEntity): AccountEntity {
    val accountId = sql
      .insertInto(ACCOUNT, ACCOUNT.USERNAME, ACCOUNT.EMAIL, ACCOUNT.PASSWORD, ACCOUNT.REGISTERED)
      .values(account.username, account.email, account.password, account.registered)
      .returning(ACCOUNT.ID)
      .fetchOne()
      .getValue(ACCOUNT.ID)
    return account.copy(id = accountId)
  }

  private fun persistNewAccountVerification(account: AccountEntity): AccountEntity {
    val token = UUID.randomUUID().toString()
    sql
      .insertInto(ACCOUNT_VERIFICATION, ACCOUNT_VERIFICATION.USERNAME, ACCOUNT_VERIFICATION.TOKEN)
      .values(account.username, token)
      .execute()
    return account.copy(verification = AccountVerification(token))
  }

  @Transactional
  fun verifyNewAccount(username: String, token: String): Boolean {
    val result = findVerifiableAccountBy(username, token)
    return when {
      result.isNotEmpty -> {
        deleteAccountVerificationFor(username)
        markAccountAsVerified(username)
        true
      }
      else -> false
    }
  }

  private fun findVerifiableAccountBy(username: String, token: String): Result<Record> {
    return sql.select()
      .from(ACCOUNT
        .join(ACCOUNT_VERIFICATION).on(ACCOUNT_VERIFICATION.USERNAME.eq(ACCOUNT.USERNAME))
      )
      .where(ACCOUNT.USERNAME.eq(username))
      .and(ACCOUNT_VERIFICATION.TOKEN.eq(token))
      .fetch()
  }

  private fun deleteAccountVerificationFor(username: String) {
    sql.deleteFrom(ACCOUNT_VERIFICATION)
      .where(ACCOUNT_VERIFICATION.USERNAME.eq(username))
      .execute()
  }

  private fun markAccountAsVerified(username: String) {
    sql.update(ACCOUNT)
      .set(ACCOUNT.VERIFIED, true)
      .where(ACCOUNT.USERNAME.eq(username))
      .execute()
  }

}