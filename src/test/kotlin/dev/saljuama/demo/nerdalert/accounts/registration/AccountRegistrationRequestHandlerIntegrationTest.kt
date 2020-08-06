package dev.saljuama.demo.nerdalert.accounts.registration

import arrow.core.Left
import arrow.core.Right
import com.ninjasquad.springmockk.MockkBean
import dev.saljuama.demo.nerdalert.accounts.Account
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*

@SpringBootTest
@AutoConfigureMockMvc
internal class AccountRegistrationRequestHandlerIntegrationTest(
  @Autowired val mockMvc: MockMvc
) {

  @MockkBean
  private lateinit var accountRegistrationService: AccountRegistrationService

  @Test
  internal fun `creating a new account returns 201`() {
    every { accountRegistrationService.createAccount(NewAccount("Pepe", "pepe@email.com", "secret")) } returns
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
    every { accountRegistrationService.createAccount(any()) } returns Left(UsernameOrEmailNotAvailableException())

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
    every { accountRegistrationService.verifyAccount("Pepe", "token") } returns Right(Account("Pepe", "email"))

    mockMvc.get("/api/accounts/Pepe/verify/token")
      .andExpect {
        status { isOk }
      }
  }

  @Test
  internal fun `validating an user with invalid token returns 400`() {
    every { accountRegistrationService.verifyAccount("Pepe", "token") } returns Left(InvalidVerificationException())

    mockMvc.get("/api/accounts/Pepe/verify/token")
      .andExpect {
        status { isBadRequest }
      }
  }
}