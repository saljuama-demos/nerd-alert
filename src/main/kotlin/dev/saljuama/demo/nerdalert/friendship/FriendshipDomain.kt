package dev.saljuama.demo.nerdalert.friendship

import arrow.core.Either
import arrow.fx.IO

data class FriendshipRequest(
  val from: String,
  val to: String
)

class UsersAreFriendsAlreadyException : Throwable()
class FriendshipRequestAlreadySentException : Throwable()

interface FriendshipService {
  fun sendFriendshipRequest(toUsername: String, fromUsername: String): Either<Throwable, Unit>
}

interface FriendshipRepository {
  fun createFriendshipRequest(toUsername: String, fromUsername: String): IO<Unit>
}