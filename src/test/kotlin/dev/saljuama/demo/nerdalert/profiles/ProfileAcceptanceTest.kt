package dev.saljuama.demo.nerdalert.profiles

import dev.saljuama.demo.nerdalert.accounts.AccountFixtures.newAccount
import dev.saljuama.demo.nerdalert.testutils.DbTestUtils
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
internal class ProfileAcceptanceTest {

  @Autowired private lateinit var mockMvc: MockMvc
  @Autowired private lateinit var sql: DSLContext

  @AfterEach
  internal fun tearDown() {
    DbTestUtils.wipeDatabase(sql)
  }

  @Test
  internal fun `updating an account and seeing the updates`() {
    DbTestUtils.createVerifiedAccount(sql, newAccount().copy(username = "Angelica"))

    mockMvc.put("/api/profiles/Angelica") {
      contentType = MediaType.APPLICATION_JSON
      content = """
          {
            "firstName": "Angelica",
            "lastName": "Awesome",
            "description": "I'm really awesome and I like animals",
            "avatar": "http://fancy.com/img.jpg"
          }
          """
    }

    mockMvc.get("/api/profiles/Angelica")
      .andExpect {
        status { isOk }
        content {
          json("""
          {
            "firstName": "Angelica",
            "lastName": "Awesome",
            "description": "I'm really awesome and I like animals",
            "avatar": "http://fancy.com/img.jpg"
          }
          """)
        }
      }
  }
}