package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Left
import arrow.core.Right
import com.ninjasquad.springmockk.MockkBean
import dev.saljuama.demo.nerdalert.accounts.AccountsFixtures.accountWithProfile
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*

@SpringBootTest
@AutoConfigureMockMvc
internal class AccountsRequestHandlerIntegrationTest(
  @Autowired val mockMvc: MockMvc
) {

  @MockkBean
  private lateinit var accountService: AccountsTransactionalService

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

  @Test
  internal fun `listing all the accounts when no accounts available returns 404`() {
    every { accountService.listAllAccounts() } returns Left(AccountNotFoundException())

    mockMvc.get("/api/accounts")
      .andExpect {
        status { isNotFound }
      }
  }

  @Test
  internal fun `listing all the accounts when accounts are found return 200`() {
    every { accountService.listAllAccounts() } returns Right(listOf(
      Account("user1", "email1"),
      Account("user2", "email2"),
      Account("user3", "email3")
    ))

    mockMvc.get("/api/accounts")
      .andExpect {
        status { isOk }
        content {
          json("""
            [
              {"username":"user1","descriptionLink":"http://localhost:8080/api/accounts/user1"},
              {"username":"user2","descriptionLink":"http://localhost:8080/api/accounts/user2"},
              {"username":"user3","descriptionLink":"http://localhost:8080/api/accounts/user3"}
            ]
            """)
        }
      }
  }

  @Test
  internal fun `viewing the details of an account that exists return 200`() {
    every { accountService.viewAccountDetails("user1") } returns Right(accountWithProfile())

    mockMvc.get("/api/accounts/user1")
      .andExpect {
        status { isOk }
        jsonPath("$.profile.firstName") { value("User") }
      }
  }

  @Test
  internal fun `viewing the details of a non-existing account returns 404`() {
    every { accountService.viewAccountDetails("user1") } returns Left(AccountNotFoundException())

    mockMvc.get("/api/accounts/user1")
      .andExpect {
        status { isNotFound }
      }
  }
}