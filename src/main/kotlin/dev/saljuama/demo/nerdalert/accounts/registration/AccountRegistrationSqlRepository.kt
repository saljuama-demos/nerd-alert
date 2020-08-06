package dev.saljuama.demo.nerdalert.accounts.registration

import arrow.fx.IO
import dev.saljuama.demo.nerdalert.accounts.AccountNotFoundException
import dev.saljuama.demos.nerdalert.Tables
import dev.saljuama.demos.nerdalert.tables.Account
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
        .insertInto(Account.ACCOUNT, Account.ACCOUNT.USERNAME, Account.ACCOUNT.EMAIL, Account.ACCOUNT.PASSWORD, Account.ACCOUNT.REGISTERED)
        .values(account.username, account.email, account.password, starterAccount.registered)
        .execute()
      sql
        .insertInto(Tables.ACCOUNT_VERIFICATION, Tables.ACCOUNT_VERIFICATION.USERNAME, Tables.ACCOUNT_VERIFICATION.TOKEN)
        .values(account.username, starterAccount.verification.token)
        .execute()
      starterAccount
    }
  }

  override fun findVerifiableAccount(username: String): IO<StarterAccount> {
    return IO {
      val result = sql
        .select()
        .from(Account.ACCOUNT.innerJoin(Tables.ACCOUNT_VERIFICATION).on(Tables.ACCOUNT_VERIFICATION.USERNAME.eq(Account.ACCOUNT.USERNAME)))
        .where(Account.ACCOUNT.USERNAME.eq(username))
        .fetchOne()
        ?: throw AccountNotFoundException()

      StarterAccount(
        result.getValue(Account.ACCOUNT.USERNAME),
        result.getValue(Account.ACCOUNT.EMAIL),
        result.getValue(Account.ACCOUNT.REGISTERED),
        AccountVerification(result.getValue(Tables.ACCOUNT_VERIFICATION.TOKEN))
      )
    }
  }

  override fun verifyAccount(account: StarterAccount): IO<Unit> {
    return IO {
      sql.update(Account.ACCOUNT)
        .set(Account.ACCOUNT.VERIFIED, true)
        .where(Account.ACCOUNT.USERNAME.eq(account.username))
        .execute()
      sql.deleteFrom(Tables.ACCOUNT_VERIFICATION)
        .where(Tables.ACCOUNT_VERIFICATION.USERNAME.eq(account.username))
        .execute()
      Unit
    }
  }

}