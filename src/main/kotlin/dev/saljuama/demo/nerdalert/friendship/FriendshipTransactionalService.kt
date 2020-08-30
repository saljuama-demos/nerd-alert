package dev.saljuama.demo.nerdalert.friendship

import arrow.core.*
import org.springframework.stereotype.Component

@Component
class FriendshipTransactionalService(
  private val friendshipRepository: FriendshipRepository
) : FriendshipService {

  override fun sendFriendshipRequest(toUsername: String, fromUsername: String): Either<Throwable, Unit> {
    return friendshipRepository.createFriendshipRequest(toUsername, fromUsername)
      .attempt()
      .unsafeRunSync()
      .handleErrorWith { error ->
        when (error) {
          is FriendshipRequestAlreadySentException -> Right(Unit)
          else -> Left(error)
        }
      }
  }

}
