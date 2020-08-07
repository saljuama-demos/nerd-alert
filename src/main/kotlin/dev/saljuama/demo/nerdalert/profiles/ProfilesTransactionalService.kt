package dev.saljuama.demo.nerdalert.profiles

import arrow.core.*
import org.springframework.stereotype.Component

@Component
class ProfilesTransactionalService(
  private val repository: ProfileRepository
) : ProfileService {

  override fun findProfile(username: String): Either<Throwable, Profile> {
    return repository.findProfile(username)
      .attempt()
      .unsafeRunSync()
      .handleErrorWith { error ->
        when (error) {
          is ProfileNotInitializedException -> Right(Profile(username))
          else -> Left(error)
        }
      }
  }

  override fun updateProfile(profile: Profile): Either<Throwable, Unit> {
    TODO("not implemented")
  }

}
