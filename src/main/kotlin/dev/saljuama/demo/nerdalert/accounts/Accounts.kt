package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Either


interface AccountsService {
  fun listAllAccounts(): Either<Throwable, List<Account>>
}
