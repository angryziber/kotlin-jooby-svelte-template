package db

import com.zaxxer.hikari.util.DriverDataSource
import db.DBModule.Companion.testDBUrl
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import org.junit.jupiter.api.extension.RegisterExtension
import java.lang.reflect.Method
import java.util.*

abstract class BaseIntegrationTest {
  companion object {
    val db = DriverDataSource(testDBUrl, null, Properties(), null, null).apply { migrate(listOf("test", "test-data")) }
  }

  @RegisterExtension @JvmField @Suppress("unused")
  val autoRollback = InTransactionRunner()

  class InTransactionRunner: InvocationInterceptor {
    override fun interceptTestMethod(invocation: InvocationInterceptor.Invocation<Void>?, invocationContext: ReflectiveInvocationContext<Method>?, extensionContext: ExtensionContext?) {
      db.withTransaction(rollbackOnly = true) {
        invocation!!.proceed()
      }
    }
  }
}
