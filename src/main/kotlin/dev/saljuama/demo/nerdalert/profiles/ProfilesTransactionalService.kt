package dev.saljuama.demo.nerdalert.profiles

import arrow.core.Either
import org.springframework.stereotype.Component

@Component
class ProfilesTransactionalService : ProfileService {

  override fun findProfile(username: String): Either<Throwable, Profile> {
    TODO("not implemented")
  }

}