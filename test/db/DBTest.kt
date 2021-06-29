package db

import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.RegisterExtension

abstract class DBTest {
  companion object {
    val db = try {
      HikariDataSource(DBModule.hikariConfig.apply { jdbcUrl = jdbcUrl.replace("/" + DBModule.dbName, "/" + DBModule.dbName + "_test") }).apply {
        migrate(listOf("test", "test-data"))
      }
    } catch (e: HikariPool.PoolInitializationException) {
      throw IllegalStateException("Test DB not running, please use `docker-compose up -d db`\n${e.message}")
    }
  }

  @RegisterExtension @JvmField @Suppress("unused")
  val autoRollback = InTransactionRunner()

  class InTransactionRunner: BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
      Transaction(db).attachToRequest()
    }

    override fun afterEach(context: ExtensionContext?) {
      Transaction.current()!!.close(commit = false)
    }
  }
}
