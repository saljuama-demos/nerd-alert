package dev.saljuama.demo.nerdalert.profiles

import arrow.core.getOrHandle
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

data class UserProfileUpdateRequest(
  val firstName: String,
  val lastName: String,
  val description: String,
  val avatar: String
) {
  fun toProfile(username: String) = Profile(username, firstName, lastName, description, avatar)
}

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

  fun updateUserProfile(request: ServerRequest): ServerResponse {
    val username = request.pathVariable("username")
    val profileData = request.body(UserProfileUpdateRequest::class.java)
    val updatedProfile = profileData.toProfile(username)

    return profileService.updateProfile(updatedProfile)
      .map { ServerResponse.accepted().build() }
      .getOrHandle { error ->
        when (error) {
          is ProfileNotFoundException -> ServerResponse.badRequest().build()
          else -> ServerResponse.status(500).build()
        }
      }
  }

  fun searchUserProfiles(request: ServerRequest): ServerResponse {
    return profileService.searchProfiles()
      .map { ServerResponse.ok().body(it) }
      .getOrHandle { ServerResponse.status(500).build() }
  }

}
