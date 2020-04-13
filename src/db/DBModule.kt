package db

import io.jooby.Extension
import io.jooby.Jooby
import io.jooby.hikari.HikariModule
import io.jooby.require
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import javax.sql.DataSource

class DBModule: Extension {
  companion object {
    const val testDBUrl = "jdbc:h2:mem:app;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;user=sa;password="
  }

  override fun install(app: Jooby) = app.run {
    if (app.environment.isActive("test"))
      useTestDB()
    else
      try {
        var jdbcUrl = System.getenv("DATABASE_URL")?.let { herokuDbUrlToJdbc(it) }
        if (jdbcUrl == null) {
          val dbHost = System.getenv("DB_HOST") ?: "127.0.0.1:55432"
          jdbcUrl = "jdbc:postgresql://${dbHost}/app?user=app&password=app"
        }
        install(HikariModule(jdbcUrl))
      }
      catch (e: Exception) {
        if (app.environment.isActive("dev")) {
          log.warn("Failed to connect to Postgresql, trying in-memory H2 instead")
          useTestDB()
        }
        else throw e
      }
    require<DataSource>().migrate(app.environment.activeNames)
  }

  fun herokuDbUrlToJdbc(herokuUrl: String): String {
    val m = "postgres://(?<user>.+?):(?<password>.+?)@(?<hostportdb>.*)".toRegex().matchEntire(herokuUrl)!!.groups
    return "jdbc:postgresql://${m["hostportdb"]!!.value}?sslmode=require&user=${m["user"]!!.value}&password=${m["password"]!!.value}"
  }

  private fun Jooby.useTestDB() = install(HikariModule(testDBUrl))
}

fun DataSource.migrate(configs: List<String>) {
  connection.use { connection ->
    val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
    val liquibase = Liquibase("db/db.xml", ClassLoaderResourceAccessor(), database)
    liquibase.update(configs.joinToString(","))
  }
}
