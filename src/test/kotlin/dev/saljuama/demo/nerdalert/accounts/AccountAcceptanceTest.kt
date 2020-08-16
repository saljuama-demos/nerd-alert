package dev.saljuama.demo.nerdalert.accounts

import dev.saljuama.demo.nerdalert.DbTestUtils
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
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
internal class AccountAcceptanceTest {

  @Autowired private lateinit var mockMvc: MockMvc
  @Autowired private lateinit var sql: DSLContext

  @AfterEach
  internal fun tearDown() {
    DbTestUtils.wipeDatabase(sql)
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

    mockMvc.get(extractVerificationRelativeUrl(result))
      .andExpect {
        status { isOk }
      }
  }

  private fun extractVerificationRelativeUrl(result: MvcResult): String {
    return result.response.contentAsString
      .replace("""{"verificationUrl":"http://localhost:8080""", "")
      .replace("\"}", "")
  }
}