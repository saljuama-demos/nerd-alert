package dev.saljuama.demo.nerdalert.profiles

import arrow.core.Either

data class Profile(
  val username: String,
  val firstName: String = "",
  val lastName: String = "",
  val description: String = "",
  val avatar: String = "https://avataaars.io/?avatarStyle=Circle&topType=ShortHairShaggyMullet&accessoriesType=Round&hairColor=Black&facialHairType=Blank&clotheType=GraphicShirt&clotheColor=Black&graphicType=Bat&eyeType=Happy&eyebrowType=Default&mouthType=Default&skinColor=Light"
)

class ProfileNotFoundException : Throwable()

interface ProfileService {
  fun findProfile(username: String): Either<Throwable, Profile>
}
