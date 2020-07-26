package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Either
import arrow.fx.extensions.io.async.effectMap
import arrow.fx.extensions.io.monad.flatTap
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class UserProfileInput(
  val username: String,
  val firstName: String,
  val lastName: String?,
  val description: String?,
  val imageUrl: String?
)

@Component
class AccountsTransactionalService(
  val repository: AccountsRepository
) : AccountsService {

  @Transactional
  override fun createAccount(newAccount: NewAccount): Either<Throwable, StarterAccount> {
    return repository.saveAccount(newAccount)
      .attempt()
      .unsafeRunSync()
  }

  @Transactional
  override fun verifyAccount(username: String, token: String): Either<Throwable, Account> {
    return repository.findVerifiableAccount(username)
      .effectMap { starterAccount -> validateToken(token, starterAccount) }
      .flatTap { starterAccount -> repository.verifyAccount(starterAccount) }
      .map { starterAccount -> Account(starterAccount.username, starterAccount.email, starterAccount.registered) }
      .attempt()
      .unsafeRunSync()
  }

  private fun validateToken(token: String, starterAccount: StarterAccount): StarterAccount {
    if (token != starterAccount.verification.token)
      throw InvalidVerificationException()
    return starterAccount
  }

  @Transactional
  override fun updateProfile(username: String, profile: UserProfile): Either<Throwable, Account> {
    return repository.findVerifiedAccount(username)
      .map { account -> account.copy(profile = profile) }
      .flatTap { repository.updateProfile(it) }
      .attempt()
      .unsafeRunSync()
  }

  @Transactional
  override fun deleteAccount(username: String): Either<Throwable, Unit> {
    return repository.deleteAccount(username)
      .attempt()
      .unsafeRunSync()
  }

//
//  @Transactional
//  fun createNewAccount(newAccount: NewAccount): Either<Throwable, Account> {
//    val account = Account(newAccount.username, newAccount.email, newAccount.password)
//    return persistNewAccount(account)
//      .flatMap { savedAccount -> persistNewAccountVerification(savedAccount) }
//      .attempt()
//      .unsafeRunSync()
//  }
//
//  private fun persistNewAccount(account: Account): IO<Account> {
//    return IO {
//      sql
//        .insertInto(ACCOUNT, ACCOUNT.USERNAME, ACCOUNT.EMAIL, ACCOUNT.PASSWORD, ACCOUNT.REGISTERED)
//        .values(account.username, account.email, account.password, account.registered)
//        .returning(ACCOUNT.ID)
//        .fetchOne()
//        .getValue(ACCOUNT.ID)
//      account
//    }
//  }
//
//  private fun persistNewAccountVerification(account: Account): IO<Account> {
//    return IO {
//      val token = UUID.randomUUID().toString()
//      sql
//        .insertInto(ACCOUNT_VERIFICATION, ACCOUNT_VERIFICATION.USERNAME, ACCOUNT_VERIFICATION.TOKEN)
//        .values(account.username, token)
//        .execute()
//      account.copy(verification = AccountVerification(token))
//    }
//  }
//
//  @Transactional
//  fun verifyNewAccount(username: String, token: String): Either<Throwable, Account> {
//    return findVerifiableAccountBy(username, token)
//      .flatTap { deleteAccountVerificationFor(username) }
//      .flatTap { markAccountAsVerified(username) }
//      .map { account -> account.copy(verified = true) }
//      .attempt()
//      .unsafeRunSync()
//  }
//
//  private fun findVerifiableAccountBy(username: String, token: String): IO<Account> {
//    return IO {
//      val queryResult = sql.select()
//        .from(ACCOUNT
//          .innerJoin(ACCOUNT_VERIFICATION)
//          .on(ACCOUNT_VERIFICATION.USERNAME.eq(ACCOUNT.USERNAME))
//        )
//        .where(ACCOUNT.USERNAME.eq(username))
//        .and(ACCOUNT_VERIFICATION.TOKEN.eq(token))
//        .fetchOne()
//
//      Account(
//        queryResult.getValue(ACCOUNT.USERNAME),
//        queryResult.getValue(ACCOUNT.EMAIL),
//        "censoredPassword",
//        queryResult.getValue(ACCOUNT.REGISTERED),
//        queryResult.getValue(ACCOUNT.VERIFIED),
//        AccountVerification(queryResult.getValue(ACCOUNT_VERIFICATION.TOKEN))
//      )
//    }
//  }
//
//  private fun deleteAccountVerificationFor(username: String): IO<Unit> {
//    return IO {
//      sql.deleteFrom(ACCOUNT_VERIFICATION)
//        .where(ACCOUNT_VERIFICATION.USERNAME.eq(username))
//        .execute()
//      Unit
//    }
//  }
//
//  private fun markAccountAsVerified(username: String): IO<Unit> {
//    return IO {
//      sql.update(ACCOUNT)
//        .set(ACCOUNT.VERIFIED, true)
//        .where(ACCOUNT.USERNAME.eq(username))
//        .execute()
//      Unit
//    }
//  }
//
//  @Transactional
//  fun createUserProfile(profile: UserProfileInput): Either<Throwable, Account> {
//    return Left(Throwable("boom"))
//  }
}
