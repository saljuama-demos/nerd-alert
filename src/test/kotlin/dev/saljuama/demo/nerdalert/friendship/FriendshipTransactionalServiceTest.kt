package dev.saljuama.demo.nerdalert.friendship

import arrow.fx.IO
import dev.saljuama.demo.nerdalert.profiles.ProfileNotFoundException
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class FriendshipTransactionalServiceTest {

  @MockK private lateinit var friendshipRepository: FriendshipRepository
  private lateinit var service: FriendshipService

  @BeforeEach
  internal fun setUp() {
    service = FriendshipTransactionalService(friendshipRepository)
  }

  @Test
  internal fun `sending a friend request to a profile that exists succeeds`() {
    every { friendshipRepository.createFriendshipRequest("Pepe", "Angelika") } returns IO.unit

    val result = service.sendFriendshipRequest("Pepe", "Angelika")

    assertTrue(result.isRight())
  }

  @Test
  internal fun `sending a friend request when the target does not exist or is not initialized fails`() {
    every { friendshipRepository.createFriendshipRequest("Pepe", "Angelika") } returns IO { throw ProfileNotFoundException() }

    val result = service.sendFriendshipRequest("Pepe", "Angelika")

    assertTrue(result.isLeft())
  }

  @Test
  internal fun `sending a friend request to a profile that there is already a friendship fails and does nothing`() {
    every { friendshipRepository.createFriendshipRequest("Pepe", "Angelika") } returns IO { throw UsersAreFriendsAlreadyException() }

    val result = service.sendFriendshipRequest("Pepe", "Angelika")

    assertTrue(result.isLeft())
  }

  @Test
  internal fun `sending a friend request to a profile that already has a request pending succeeds and does nothing`() {
    every { friendshipRepository.createFriendshipRequest("Pepe", "Angelika") } returns IO { throw FriendshipRequestAlreadySentException() }

    val result = service.sendFriendshipRequest("Pepe", "Angelika")

    assertTrue(result.isRight())
  }

}
