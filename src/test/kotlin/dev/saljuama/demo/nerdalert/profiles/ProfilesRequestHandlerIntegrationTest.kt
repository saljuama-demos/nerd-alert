package dev.saljuama.demo.nerdalert.profiles

import arrow.core.Left
import arrow.core.Right
import com.ninjasquad.springmockk.MockkBean
import dev.saljuama.demo.nerdalert.profiles.ProfilesFixtures.defaultProfile
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
internal class ProfilesRequestHandlerIntegrationTest {

  @MockkBean private lateinit var profilesService: ProfileService
  @Autowired private lateinit var mockMvc: MockMvc

  @Test
  internal fun `viewing profile details returns 200 and the values`() {
    every { profilesService.findProfile("Pepe") } returns
      Right(defaultProfile().copy(username = "Pepe", avatar = "http://avatar.com/img.jpg"))

    mockMvc.get("/api/profiles/Pepe")
      .andExpect {
        status { isOk }
        content {
          json("""{
          | "username": "Pepe",
          | "firstName": "",
          | "lastName": "",
          | "description": "",
          | "avatar": "http://avatar.com/img.jpg"
          |}""".trimMargin())
        }
      }
  }

  @Test
  internal fun `viewing profile for non existing user returns 404`() {
    every { profilesService.findProfile("Pepe") } returns Left(ProfileNotFoundException())

    mockMvc.get("/api/profiles/Pepe")
      .andExpect {
        status { isNotFound }
      }
  }
}
