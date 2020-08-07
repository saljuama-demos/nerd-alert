package dev.saljuama.demo.nerdalert.profiles

import arrow.core.Left
import arrow.core.Right
import com.ninjasquad.springmockk.MockkBean
import dev.saljuama.demo.nerdalert.profiles.ProfileFixtures.defaultProfile
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
internal class ProfileRequestHandlerIntegrationTest {

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

  @Test
  internal fun `updating a profile for a non existing account returns 400`() {
    every { profilesService.updateProfile(Profile("Pepe", "Pepe", "Romero", "secret", "http://fancy.com/img.jpg")) } returns
      Left(ProfileNotFoundException())

    mockMvc.put("/api/profiles/Pepe") {
      contentType = MediaType.APPLICATION_JSON
      content = """
          {
            "firstName": "Pepe",
            "lastName": "Romero",
            "description": "secret",
            "avatar": "http://fancy.com/img.jpg"
          }
          """
    }.andExpect {
      status { isBadRequest }
    }
  }

  @Test
  internal fun `updating a profile for an existing account returns 202`() {
    every { profilesService.updateProfile(Profile("Pepe", "Pepe", "Romero", "secret", "http://fancy.com/img.jpg")) } returns
      Right(Unit)

    mockMvc.put("/api/profiles/Pepe") {
      contentType = MediaType.APPLICATION_JSON
      content = """
          {
            "firstName": "Pepe",
            "lastName": "Romero",
            "description": "secret",
            "avatar": "http://fancy.com/img.jpg"
          }
          """
    }.andExpect {
      status { isAccepted }
    }
  }
}
