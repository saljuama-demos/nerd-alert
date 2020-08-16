package dev.saljuama.demo.nerdalert.friendship

import arrow.core.getOrHandle
import dev.saljuama.demo.nerdalert.accounts.AccountNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse


@Component
class FriendshipRequestHandler(
  private val friendshipService: FriendshipService
) {

  fun createNewFriendshipRequest(request: ServerRequest): ServerResponse {
    val toUsername = request.pathVariable("username")
    val fromUsername = SecurityContextHolder.getContext().authentication.principal.toString()

    return friendshipService.createFriendshipRequest(toUsername, fromUsername)
      .map { ServerResponse.status(201).build() }
      .getOrHandle { error ->
        when (error) {
          is AccountNotFoundException -> ServerResponse.badRequest().build()
          else -> ServerResponse.status(500).build()
        }
      }
  }

}
