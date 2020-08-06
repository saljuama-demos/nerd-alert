package dev.saljuama.demo.nerdalert.profiles

import arrow.core.getOrHandle
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class ProfilesRequestHandler(
  private val profileService: ProfileService
) {

  fun viewUserProfile(request: ServerRequest): ServerResponse {
    val username = request.pathVariable("username")

    return profileService.findProfile(username)
      .map { ServerResponse.ok().body(it) }
      .getOrHandle { error ->
        when (error) {
          is ProfileNotFoundException -> ServerResponse.notFound().build()
          else -> ServerResponse.status(500).build()
        }
      }
  }

}