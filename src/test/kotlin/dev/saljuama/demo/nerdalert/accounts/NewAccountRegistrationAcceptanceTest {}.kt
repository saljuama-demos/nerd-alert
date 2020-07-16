package dev.saljuama.demo.nerdalert.accounts

import dev.saljuama.demos.nerdalert.Tables.ACCOUNT
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


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
  }

  @Test
  internal fun `create new account`() {
    mockMvc.perform(
      post("/api/accounts")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          """{
          |  "username": "CurroRomero",
          |  "email": "curro.romero@email.com",
          |  "password": "super secret"
          |}""".trimMargin())
    )
      .andExpect(status().isCreated)

    val accountsForUserInDb = sql.selectFrom(ACCOUNT).where(ACCOUNT.USERNAME.eq("CurroRomero")).count()
    assertEquals(1, accountsForUserInDb)
  }
}