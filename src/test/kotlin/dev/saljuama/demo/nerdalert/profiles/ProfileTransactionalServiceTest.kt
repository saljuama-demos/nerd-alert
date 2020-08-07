package dev.saljuama.demo.nerdalert.profiles

import arrow.core.getOrHandle
import arrow.fx.IO
import dev.saljuama.demo.nerdalert.profiles.ProfileFixtures.profile
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ProfileTransactionalServiceTest {

  @MockK private lateinit var repository: ProfileRepository
  private lateinit var service: ProfileService

  @BeforeEach
  fun setUp() {
    service = ProfileTransactionalService(repository)
  }

  @Test
  internal fun `finding a profile for an existing account without initialized profile returns the default profile`() {
    val username = "Pepe"
    every { repository.findProfile(username) } returns IO { throw ProfileNotInitializedException() }

    val result = service.findProfile(username)

    result
      .map {
        assertEquals(username, it.username)
        assertEquals("", it.firstName)
        assertEquals("", it.lastName)
        assertEquals("", it.description)
        assertEquals(defaultAvatar, it.avatar)
      }
      .getOrHandle { fail() }
  }

  @Test
  internal fun `finding a profile for an existing account and initialized profile returns the profile`() {
    every { repository.findProfile("Pepe") } returns IO { profile() }

    val result = service.findProfile("Pepe")

    result
      .map { assertEquals(profile(), it) }
      .getOrHandle { fail() }
  }

  @Test
  internal fun `finding a profile for a non existing account returns an error`() {
    every { repository.findProfile("Pepe") } returns IO { throw ProfileNotFoundException() }

    val result = service.findProfile("Pepe")

    assertTrue(result.isLeft())
  }

  @Test
  internal fun `updating a profile for an existing account upsert the profile and returns success`() {
    every { repository.upsertProfile(profile()) } returns IO.unit

    val result = service.updateProfile(profile())

    assertTrue(result.isRight())
  }

  @Test
  internal fun `updating a profile for a non existing account returns an error`() {
    every { repository.upsertProfile(profile()) } returns IO { throw ProfileNotFoundException() }

    val result = service.updateProfile(profile())

    assertTrue(result.isLeft())
  }
}
