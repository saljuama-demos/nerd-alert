package dev.saljuama.demo.nerdalert.testutils

import dev.saljuama.demos.nerdalert.Tables
import org.jooq.*

object DbTestUtils {

  fun wipeDatabase(sql: DSLContext) {
    sql.deleteFrom(Tables.ACCOUNT).execute()
    sql.deleteFrom(Tables.ACCOUNT_VERIFICATION).execute()
    sql.deleteFrom(Tables.USER_PROFILE).execute()
  }

  fun wipeTables(sql: DSLContext, tables: List<Table<out Record>>) {
    tables.forEach { sql.deleteFrom(it).execute() }
  }

}
