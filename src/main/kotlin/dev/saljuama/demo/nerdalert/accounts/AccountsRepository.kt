package dev.saljuama.demo.nerdalert.accounts

import dev.saljuama.demos.nerdalert.tables.Account.ACCOUNT
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.LocalDate

data class AccountEntity(
  val id: Long?,
  val username: String,
  val email: String,
  val password: String,
  val registered: LocalDate = LocalDate.now()
)

@Component
class AccountsRepository(
  val sql: DSLContext
) {

  fun createNewAccount(account: AccountEntity): Boolean {
    return try {
      sql.insertInto(ACCOUNT, ACCOUNT.USERNAME, ACCOUNT.EMAIL, ACCOUNT.PASSWORD, ACCOUNT.REGISTERED)
        .values(account.username, account.email, account.password, account.registered)
        .execute()
      true
    } catch (e: Exception) {
      false
    }
  }

}