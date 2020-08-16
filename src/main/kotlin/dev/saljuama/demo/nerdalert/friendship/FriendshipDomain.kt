package dev.saljuama.demo.nerdalert.friendship

import arrow.core.Either

interface FriendshipService {
  fun createFriendshipRequest(toUsername: String, fromUsername: String): Either<Throwable, Unit>
}
