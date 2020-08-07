package dev.saljuama.demo.nerdalert.profiles

import arrow.core.Either
import arrow.fx.IO

const val defaultAvatar = "https://avataaars.io/?avatarStyle=Circle&topType=ShortHairShaggyMullet&accessoriesType=Round&hairColor=Black&facialHairType=Blank&clotheType=GraphicShirt&clotheColor=Black&graphicType=Bat&eyeType=Happy&eyebrowType=Default&mouthType=Default&skinColor=Light"

data class Profile(
  val username: String,
  val firstName: String = "",
  val lastName: String = "",
  val description: String = "",
  val avatar: String = defaultAvatar
)

class ProfileNotFoundException : Throwable()
class ProfileNotInitializedException : Throwable()

interface ProfileService {
  fun findProfile(username: String): Either<Throwable, Profile>
  fun updateProfile(profile: Profile): Either<Throwable, Unit>
}

interface ProfileRepository {
  fun findProfile(username: String): IO<Profile>
}
