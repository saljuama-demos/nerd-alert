package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Left
import arrow.core.Right
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import dev.saljuama.demo.nerdalert.RoutesConfiguration
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
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

  @SpykBean
  lateinit var accountsRequestHandler: AccountsRequestHandler

  @MockkBean
  lateinit var accountRepository: AccountsRepository

  @BeforeEach
  internal fun setUp() {
    accountsRequestHandler = AccountsRequestHandler(accountRepository)
  }

  @Test
  internal fun `creating a new account with available username and email, return a verification url`() {
    val accountEntity = AccountEntity(null, "Pepe", "pepe@email.com", "secret")
    every { accountRepository.createNewAccount(accountEntity) } returns Right(accountEntity.copy(id = 1, verification = AccountVerification("token")))

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
  internal fun `creating a new account with username and-or email already in use, return bad request`() {
    every { accountRepository.createNewAccount(any()) } returns Left(Throwable("boom"))

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
  internal fun `validating an user with the correct username and token combination, return OK`() {
    every { accountRepository.verifyNewAccount("Pepe", "token") } returns Right(AccountEntity(null, "Pepe", "email", "pass"))

    mockMvc.get("/api/accounts/Pepe/verify/token")
      .andExpect {
        status { isOk }
      }
  }

  @Test
  internal fun `validating an user with incorrect username and token combination, return Bad Request`() {
    every { accountRepository.verifyNewAccount("Pepe", "token") } returns Left(Throwable("boom"))

    mockMvc.get("/api/accounts/Pepe/verify/token")
      .andExpect {
        status { isBadRequest }
      }
  }
}