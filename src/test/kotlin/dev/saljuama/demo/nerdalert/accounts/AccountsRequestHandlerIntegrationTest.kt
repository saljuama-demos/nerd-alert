package dev.saljuama.demo.nerdalert.accounts

import arrow.core.Left
import arrow.core.Right
import com.ninjasquad.springmockk.MockkBean
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
internal class AccountsRequestHandlerIntegrationTest {

  @MockkBean private lateinit var accountService: AccountsService
  @Autowired private lateinit var mockMvc: MockMvc

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

}
