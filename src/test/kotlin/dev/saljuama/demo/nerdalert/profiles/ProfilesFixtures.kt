package dev.saljuama.demo.nerdalert.profiles

object ProfilesFixtures {
  val username = "Pepe"
  val firstName = "Pepe"
  val lastName = "Jeans"
  val description = "Denim pants are cool"
  val avatar = "https://avataaars.io/?avatarStyle=Circle"

  fun defaultProfile(): Profile = Profile(username)
  fun profile(): Profile = Profile(username, firstName, lastName, description, avatar)
}
