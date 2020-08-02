package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Left
import arrow.core.Right
import com.ninjasquad.springmockk.MockkBean
import dev.saljuama.demo.nerdalert.RoutesConfiguration
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*

@WebMvcTest
@Import(RoutesConfiguration::class)
internal class AccountsRequestHandlerIntegrationTest(
  @Autowired val mockMvc: MockMvc
) {

  @MockkBean lateinit var accountService: AccountsTransactionalService

  @Test
  internal fun `creating a new account returns 201`() {
    every { accountService.createAccount(NewAccount("Pepe", "pepe@email.com", "secret")) } returns
      Right(StarterAccount("Pepe", "pepe@email.com", verification = AccountVerification("token")))

    mockMvc.post("/api/accounts") {
      contentType = MediaType.APPLICATION_JSON
      content = """
          {
            "username": "Pepe",
            "email": "pepe@email.com",
            "password": "secret"
          }
          """
    }.andExpect {
      status { isCreated }
      jsonPath("$.verificationUrl") { value("http://localhost:8080/api/accounts/Pepe/verify/token") }
    }
  }

  @Test
  internal fun `creating a new account when username or email not available returns 400`() {
    every { accountService.createAccount(any()) } returns Left(UsernameOrEmailNotAvailableException())

    mockMvc.post("/api/accounts") {
      contentType = MediaType.APPLICATION_JSON
      content = """
          {
            "username": "Pepe",
            "email": "pepe@email.com",
            "password": "secret"
          }
          """
    }.andExpect {
      status { isBadRequest }
      jsonPath("$.error") { value("username and/or email not available") }
    }
  }

  @Test
  internal fun `validating an user returns 200`() {
    every { accountService.verifyAccount("Pepe", "token") } returns Right(Account("Pepe", "email"))

    mockMvc.get("/api/accounts/Pepe/verify/token")
      .andExpect {
        status { isOk }
      }
  }

  @Test
  internal fun `validating an user with invalid token returns 400`() {
    every { accountService.verifyAccount("Pepe", "token") } returns Left(InvalidVerificationException())

    mockMvc.get("/api/accounts/Pepe/verify/token")
      .andExpect {
        status { isBadRequest }
      }
  }

  @Test
  internal fun `creating a new user profile returns 201`() {
    every { accountService.updateProfile(any(), any()) } returns
      Right(Account("Pepe", "email", profile = UserProfile("Pepe", "Romero", "secret", "http://fancy.com/img.jpg")))

    mockMvc.post("/api/accounts/Pepe/profile") {
      contentType = MediaType.APPLICATION_JSON
      content = """
          {
            "firstName": "Pepe",
            "lastName": "Romero",
            "description": "secret",
            "imageUrl": "http://fancy.com/img.jpg"
          }
          """
    }.andExpect {
      status { isCreated }
    }
  }

  @Test
  internal fun `creating a new account with missing required fields returns 400`() {
    mockMvc.post("/api/accounts/Pepe/profile") {
      contentType = MediaType.APPLICATION_JSON
      content = """
          {
            "lastName": "Romero",
            "description": "secret",
            "imageUrl": "http://fancy.com/img.jpg"
          }
          """
    }.andExpect {
      status { isBadRequest }
    }
  }

  @Test
  internal fun `creating a new user profile with not verified account returns 400`() {
    every { accountService.updateProfile(any(), any()) } returns Left(AccountNotFoundException())

    mockMvc.post("/api/accounts/Pepe/profile") {
      contentType = MediaType.APPLICATION_JSON
      content = """
          {
            "firstName": "Pepe",
            "lastName": "Romero",
            "description": "secret",
            "imageUrl": "http://fancy.com/img.jpg"
          }
          """
    }.andExpect {
      status { isBadRequest }
      jsonPath("$.error") { value("account not found or not verified") }
    }
  }

}