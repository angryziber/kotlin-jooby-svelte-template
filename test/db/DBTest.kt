package db

import com.zaxxer.hikari.util.DriverDataSource
import db.DBModule.Companion.testDBUrl
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.*

abstract class DBTest {
  companion object {
    val db = DriverDataSource(testDBUrl, null, Properties(), null, null).apply { migrate(listOf("test", "test-data")) }
  }

  @RegisterExtension @JvmField @Suppress("unused")
  val autoRollback = InTransactionRunner()

  class InTransactionRunner: BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
      Transaction(db)
    }

    override fun afterEach(context: ExtensionContext?) {
      Transaction.current()!!.close(commit = false)
    }
  }
}
