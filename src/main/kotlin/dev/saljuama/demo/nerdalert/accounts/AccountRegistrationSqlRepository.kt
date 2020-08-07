package dev.saljuama.demo.nerdalert.accounts

import arrow.fx.IO
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT_VERIFICATION
import dev.saljuama.demos.nerdalert.tables.Account.ACCOUNT
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class AccountRegistrationSqlRepository(
  private val sql: DSLContext
) : AccountRegistrationRepository {

  override fun saveAccount(account: NewAccount): IO<StarterAccount> {
    return IO {
      val starterAccount = StarterAccount(account.username, account.email)
      sql
        .insertInto(ACCOUNT, ACCOUNT.USERNAME, ACCOUNT.EMAIL, ACCOUNT.PASSWORD, ACCOUNT.REGISTERED)
        .values(account.username, account.email, account.password, starterAccount.registered)
        .execute()
      sql
        .insertInto(ACCOUNT_VERIFICATION, ACCOUNT_VERIFICATION.USERNAME, ACCOUNT_VERIFICATION.TOKEN)
        .values(account.username, starterAccount.verification.token)
        .execute()
      starterAccount
    }
  }

  override fun findVerifiableAccount(username: String): IO<StarterAccount> {
    return IO {
      val result = sql
        .select()
        .from(ACCOUNT.innerJoin(ACCOUNT_VERIFICATION).on(ACCOUNT_VERIFICATION.USERNAME.eq(ACCOUNT.USERNAME)))
        .where(ACCOUNT.USERNAME.eq(username))
        .fetchOne()
        ?: throw AccountNotFoundException()

      StarterAccount(
        result.getValue(ACCOUNT.USERNAME),
        result.getValue(ACCOUNT.EMAIL),
        result.getValue(ACCOUNT.REGISTERED),
        AccountVerification(result.getValue(ACCOUNT_VERIFICATION.TOKEN))
      )
    }
  }

  override fun verifyAccount(account: StarterAccount): IO<Unit> {
    return IO {
      sql.update(ACCOUNT)
        .set(ACCOUNT.VERIFIED, true)
        .where(ACCOUNT.USERNAME.eq(account.username))
        .execute()
      sql.deleteFrom(ACCOUNT_VERIFICATION)
        .where(ACCOUNT_VERIFICATION.USERNAME.eq(account.username))
        .execute()
      Unit
    }
  }

  override fun deleteAccount(username: String): IO<Unit> {
    return IO {
      sql.selectFrom(ACCOUNT).where(ACCOUNT.USERNAME.eq(username)).fetchOne()
        ?: throw AccountNotFoundException()
      sql.deleteFrom(ACCOUNT).where(ACCOUNT.USERNAME.eq(username)).execute()
      Unit
    }
  }

}