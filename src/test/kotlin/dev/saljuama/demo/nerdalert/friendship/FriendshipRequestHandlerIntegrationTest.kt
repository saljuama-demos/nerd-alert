package dev.saljuama.demo.nerdalert.friendship

import arrow.core.Left
import arrow.core.Right
import com.ninjasquad.springmockk.MockkBean
import dev.saljuama.demo.nerdalert.JwtTestUtils
import dev.saljuama.demo.nerdalert.profiles.ProfileNotFoundException
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
class FriendshipRequestHandlerIntegrationTest {

  @MockkBean private lateinit var service: FriendshipService
  @Autowired private lateinit var mockMvc: MockMvc

  @Test
  internal fun `sending a friend request to someone returns a 201`() {
    every { service.sendFriendshipRequest("Pepe", "Angelika") } returns Right(Unit)

    mockMvc.post("/api/friendship/Pepe") {
      header("Authorization", "Bearer ${JwtTestUtils.generateAuthToken("Angelika")}")
    }.andExpect {
      status { isCreated }
    }
  }

  @Test
  internal fun `sending a friend request to someone who does not exist returns a 404`() {
    every { service.sendFriendshipRequest("Pepe", "Angelika") } returns Left(ProfileNotFoundException())

    mockMvc.post("/api/friendship/Pepe") {
      header("Authorization", "Bearer ${JwtTestUtils.generateAuthToken("Angelika")}")
    }.andExpect {
      status { isBadRequest }
    }
  }
}