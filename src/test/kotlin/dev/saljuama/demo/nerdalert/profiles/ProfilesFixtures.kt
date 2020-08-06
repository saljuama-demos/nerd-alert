package dev.saljuama.demo.nerdalert.profiles

object ProfilesFixtures {
  val username = "Pepe"
  val firstName = ""
  val lastName = ""
  val description = ""
  val avatar = "https://avataaars.io/?avatarStyle=Circle"

  fun defaultProfile(): Profile = Profile(username, firstName, lastName, description, avatar)
}
