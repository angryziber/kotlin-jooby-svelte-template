package db

import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

class Transaction(private val db: DataSource) {
  private var conn: Connection? = null

  val connection: Connection
    get() = conn ?: db.connection.also { it.autoCommit = false; conn = it }

  fun close(commit: Boolean) {
    conn?.apply {
      if (commit) commit() else rollback()
      autoCommit = true
      close()
    }
    conn = null
  }
}

private val threadContext = ThreadLocal<Transaction>()

fun <R> DataSource.withConnection(block: Connection.() -> R): R {
  val tx = threadContext.get()
  try {
    return if (tx != null) tx.connection.block()
    else connection.use(block)
  }
  catch (e: SQLException) {
    throw if (e.message?.contains("unique constraint") == true) AlreadyExistsException(e) else e
  }
}

fun <R> DataSource.withTransaction(rollbackOnly: Boolean = false, block: () -> R): R {
  if (threadContext.get() != null) throw IllegalStateException("tx is already active")
  val tx = Transaction(this)
  try {
    threadContext.set(tx)
    return block()
  }
  catch (e: Exception) {
    tx.close(false)
    throw e
  }
  finally {
    tx.close(!rollbackOnly)
    threadContext.remove()
  }
}
