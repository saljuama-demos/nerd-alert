package dev.saljuama.demo.nerdalert.accounts

import arrow.fx.IO
import dev.saljuama.demos.nerdalert.tables.Account.ACCOUNT
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class AccountsSqlRepository(
  private val sql: DSLContext
) : AccountsRepository {

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
