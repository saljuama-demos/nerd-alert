package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Either
import arrow.fx.IO
import arrow.fx.extensions.io.monad.flatTap
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT_VERIFICATION
import org.jooq.DSLContext
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
  val verified: Boolean = false,
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
  fun createNewAccount(account: AccountEntity): Either<Throwable, AccountEntity> {
    return IO {
      val savedAccount = persistNewAccount(account)
      persistNewAccountVerification(savedAccount)
    }
      .attempt()
      .unsafeRunSync()
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
  fun verifyNewAccount(username: String, token: String): Either<Throwable, AccountEntity> {
    return findVerifiableAccountBy(username, token)
      .flatTap { deleteAccountVerificationFor(username) }
      .flatTap { markAccountAsVerified(username) }
      .map { account -> account.copy(verified = true) }
      .attempt()
      .unsafeRunSync()
  }

  private fun findVerifiableAccountBy(username: String, token: String): IO<AccountEntity> {
    return IO {
      val queryResult = sql.select()
        .from(ACCOUNT
          .innerJoin(ACCOUNT_VERIFICATION)
          .on(ACCOUNT_VERIFICATION.USERNAME.eq(ACCOUNT.USERNAME))
        )
        .where(ACCOUNT.USERNAME.eq(username))
        .and(ACCOUNT_VERIFICATION.TOKEN.eq(token))
        .fetchOne()

      AccountEntity(
        queryResult.getValue(ACCOUNT.ID),
        queryResult.getValue(ACCOUNT.USERNAME),
        queryResult.getValue(ACCOUNT.EMAIL),
        "censoredPassword",
        queryResult.getValue(ACCOUNT.REGISTERED),
        queryResult.getValue(ACCOUNT.VERIFIED),
        AccountVerification(queryResult.getValue(ACCOUNT_VERIFICATION.TOKEN))
      )
    }
  }

  private fun deleteAccountVerificationFor(username: String): IO<Unit> {
    return IO {
      sql.deleteFrom(ACCOUNT_VERIFICATION)
        .where(ACCOUNT_VERIFICATION.USERNAME.eq(username))
        .execute()
      Unit
    }
  }

  private fun markAccountAsVerified(username: String): IO<Unit> {
    return IO {
      sql.update(ACCOUNT)
        .set(ACCOUNT.VERIFIED, true)
        .where(ACCOUNT.USERNAME.eq(username))
        .execute()
      Unit
    }
  }

}