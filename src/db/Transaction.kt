package db

import io.jooby.RequestScope
import kotlinx.coroutines.ThreadContextElement
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class Transaction(private val db: DataSource) {
  companion object {
    fun current(): Transaction? = RequestScope.get("tx")
  }

  private var conn: Connection? = null

  val connection: Connection
    get() = conn ?: db.connection.also { it.autoCommit = false; conn = it }

  fun close(commit: Boolean) {
    try {
      conn?.apply {
        if (commit) commit() else rollback()
        autoCommit = true
        close()
      }
    } finally {
      conn = null
      detachFromRequest()
    }
  }

  fun attachToRequest() = this.also { RequestScope.bind("tx", it) }
  fun detachFromRequest() = RequestScope.unbind<Transaction?>("tx")
}

fun <R> DataSource.withConnection(block: Connection.() -> R): R {
  val tx = Transaction.current()
  return try {
    if (tx != null) tx.connection.block()
    else connection.use(block)
  }
  catch (e: SQLException) {
    throw if (e.message?.contains("unique constraint") == true) AlreadyExistsException(e) else e
  }
}

class TransactionCoroutineContext(private val tx: Transaction? = Transaction.current()): ThreadContextElement<Transaction?>, AbstractCoroutineContextElement(Key) {
  companion object Key: CoroutineContext.Key<TransactionCoroutineContext>

  override fun updateThreadContext(context: CoroutineContext) = tx?.attachToRequest()
  override fun restoreThreadContext(context: CoroutineContext, oldState: Transaction?) { oldState?.attachToRequest() }
}
