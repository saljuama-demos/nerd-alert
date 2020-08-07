package dev.saljuama.demo.nerdalert.profiles

import arrow.core.*
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProfileTransactionalService(
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

  @Transactional
  override fun updateProfile(profile: Profile): Either<Throwable, Unit> {
    return repository.upsertProfile(profile)
      .attempt()
      .unsafeRunSync()
  }

}
