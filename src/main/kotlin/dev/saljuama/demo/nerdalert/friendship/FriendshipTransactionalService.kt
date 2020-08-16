package dev.saljuama.demo.nerdalert.friendship

import arrow.core.Either
import org.springframework.stereotype.Component

@Component
class FriendshipTransactionalService : FriendshipService {

  override fun createFriendshipRequest(toUsername: String, fromUsername: String): Either<Throwable, Unit> {
    TODO("not implemented")
  }

}
