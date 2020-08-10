package dev.saljuama.demo.nerdalert

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
internal class AuthenticationEndpointsIntegrationTest {

  @MockkBean lateinit var jwtTokenFactory: JwtTokenFactory
  @Autowired lateinit var mockMvc: MockMvc

  @Test
  internal fun `login with correct credentials returns 200 and a token`() {
    every { jwtTokenFactory.generateTokenForUser("user1") } returns "a-fancy-token"

    mockMvc.post("/login") {
      contentType = MediaType.APPLICATION_FORM_URLENCODED
      content = loginForm("user1", "secret")
    }.andExpect {
      status { isOk }
      header { string("Authorization", "Bearer a-fancy-token") }
    }
  }

  @Test
  internal fun `login with incorrect credentials returns 401`() {
    mockMvc.post("/login") {
      contentType = MediaType.APPLICATION_FORM_URLENCODED
      content = loginForm("incorrect-user", "secret")
    }.andExpect {
      status { isUnauthorized }
    }
  }


  fun loginForm(username: String, password: String): String {
    val result = StringBuilder()
    return result
      .append(URLEncoder.encode("username", StandardCharsets.UTF_8.name()))
      .append("=")
      .append(URLEncoder.encode(username, StandardCharsets.UTF_8.name()))
      .append("&")
      .append(URLEncoder.encode("password", StandardCharsets.UTF_8.name()))
      .append("=")
      .append(URLEncoder.encode(password, StandardCharsets.UTF_8.name()))
      .toString()
  }
}