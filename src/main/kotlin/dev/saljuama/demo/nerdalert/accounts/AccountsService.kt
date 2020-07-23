package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Either
import arrow.core.Left
import arrow.fx.IO
import arrow.fx.extensions.io.monad.flatTap
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT_VERIFICATION
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

data class NewAccount(
  val username: String,
  val email: String,
  val password: String
)

data class UserProfileInput(
  val username: String,
  val firstName: String,
  val lastName: String?,
  val description: String?,
  val imageUrl: String?
)

@Component
class AccountsService(
  val sql: DSLContext
) {

  @Transactional
  fun createNewAccount(newAccount: NewAccount): Either<Throwable, Account> {
    val account = Account(newAccount.username, newAccount.email, newAccount.password)
    return persistNewAccount(account)
      .flatMap { savedAccount -> persistNewAccountVerification(savedAccount) }
      .attempt()
      .unsafeRunSync()
  }

  private fun persistNewAccount(account: Account): IO<Account> {
    return IO {
      sql
        .insertInto(ACCOUNT, ACCOUNT.USERNAME, ACCOUNT.EMAIL, ACCOUNT.PASSWORD, ACCOUNT.REGISTERED)
        .values(account.username, account.email, account.password, account.registered)
        .returning(ACCOUNT.ID)
        .fetchOne()
        .getValue(ACCOUNT.ID)
      account
    }
  }

  private fun persistNewAccountVerification(account: Account): IO<Account> {
    return IO {
      val token = UUID.randomUUID().toString()
      sql
        .insertInto(ACCOUNT_VERIFICATION, ACCOUNT_VERIFICATION.USERNAME, ACCOUNT_VERIFICATION.TOKEN)
        .values(account.username, token)
        .execute()
      account.copy(verification = AccountVerification(token))
    }
  }

  @Transactional
  fun verifyNewAccount(username: String, token: String): Either<Throwable, Account> {
    return findVerifiableAccountBy(username, token)
      .flatTap { deleteAccountVerificationFor(username) }
      .flatTap { markAccountAsVerified(username) }
      .map { account -> account.copy(verified = true) }
      .attempt()
      .unsafeRunSync()
  }

  private fun findVerifiableAccountBy(username: String, token: String): IO<Account> {
    return IO {
      val queryResult = sql.select()
        .from(ACCOUNT
          .innerJoin(ACCOUNT_VERIFICATION)
          .on(ACCOUNT_VERIFICATION.USERNAME.eq(ACCOUNT.USERNAME))
        )
        .where(ACCOUNT.USERNAME.eq(username))
        .and(ACCOUNT_VERIFICATION.TOKEN.eq(token))
        .fetchOne()

      Account(
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

  @Transactional
  fun createUserProfile(profile: UserProfileInput): Either<Throwable, Account> {
    return Left(Throwable("boom"))
  }

}