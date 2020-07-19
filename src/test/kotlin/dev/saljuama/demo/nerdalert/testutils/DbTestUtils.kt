package dev.saljuama.demo.nerdalert.testutils

import dev.saljuama.demos.nerdalert.Tables
import org.jooq.DSLContext

object DbTestUtils {

  fun cleanupDatabase(sql: DSLContext) {
    sql.deleteFrom(Tables.ACCOUNT).execute()
    sql.deleteFrom(Tables.ACCOUNT_VERIFICATION).execute()
  }

}