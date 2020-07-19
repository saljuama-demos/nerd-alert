package dev.saljuama.demo.nerdalert.accounts

import dev.saljuama.demos.nerdalert.Tables.ACCOUNT
import dev.saljuama.demos.nerdalert.Tables.ACCOUNT_VERIFICATION
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
internal class NewAccountRegistrationAcceptanceTest(
  @Autowired val mockMvc: MockMvc,
  @Autowired val sql: DSLContext
) {

  @AfterEach
  internal fun tearDown() {
    sql.deleteFrom(ACCOUNT).execute()
    sql.deleteFrom(ACCOUNT_VERIFICATION).execute()
  }

  @Test
  internal fun `create and verify an account`() {
    val result = mockMvc.post("/api/accounts") {
      contentType = MediaType.APPLICATION_JSON
      content = """
          {
            "username": "CurroRomero",
            "email": "curro.romero@email.com",
            "password": "super secret"
          }
          """
    }.andExpect {
      status { isCreated }
    }.andReturn()

    val verificationUrl = result.response.contentAsString
      .replace("""{"verificationUrl":"http://localhost:8080""", "")
      .replace("\"}", "")

    mockMvc.get(verificationUrl)
      .andExpect {
        status { isOk }
      }
  }
}