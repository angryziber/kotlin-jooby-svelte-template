package db

import com.zaxxer.hikari.HikariConfig
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
    const val dbName = "app"
    val hikariConfig = HikariConfig().apply {
      jdbcUrl = System.getenv("DATABASE_URL")?.let { herokuDbUrlToJdbc(it) } ?: "jdbc:postgresql://${System.getenv("DB_HOST") ?: "localhost:65432"}/$dbName?user=$dbName&password=$dbName"
    }

    fun herokuDbUrlToJdbc(url: String): String {
      val m = "postgres://(?<user>.+?):(?<password>.+?)@(?<hostportdb>.*)".toRegex().matchEntire(url)?.groups ?: return url
      return "jdbc:postgresql://${m["hostportdb"]!!.value}?sslmode=require&user=${m["user"]!!.value}&password=${m["password"]!!.value}"
    }
  }

  override fun install(app: Jooby) = app.run {
    install(HikariModule(hikariConfig))
    require<DataSource>().migrate(app.environment.activeNames)
  }
}

fun DataSource.migrate(configs: List<String>) {
  connection.use { conn ->
    val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(conn))
    val liquibase = Liquibase("db/db.xml", ClassLoaderResourceAccessor(), database)
    // liquibase.dropAll() - use this to start from scratch quickly
    liquibase.update(configs.joinToString(","))
  }
}
