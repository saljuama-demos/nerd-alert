package dev.saljuama.demo.nerdalert.accounts

import dev.saljuama.demos.nerdalert.Tables.ACCOUNT
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT_VERIFICATION
import org.jooq.DSLContext
import org.springframework.stereotype.Component
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
      .insertInto(ACCOUNT_VERIFICATION, ACCOUNT_VERIFICATION.ACCOUNT, ACCOUNT_VERIFICATION.TOKEN)
      .values(account.username, token)
      .execute()
    return account.copy(verification = AccountVerification(token))
  }

}