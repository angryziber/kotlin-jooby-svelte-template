package app

import db.Transaction
import io.jooby.*
import kotlinx.coroutines.ThreadContextElement
import javax.sql.DataSource
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class RequestTransactionHandler: Extension {
  private val canonicalHost = System.getenv("CANONICAL_HOST")

  override fun install(app: Jooby): Unit = (app as Kooby).run {
    val db = require<DataSource>()

    before {
      checkHost(ctx, environment.isHttps, canonicalHost)
      val tx = Transaction(db).also { ctx.attribute("tx", it) }
      if (!ctx.isInIoThread) tx.attachToThread()
      ctx.onComplete {
        it.attribute<Transaction>("tx").close(commit = (200..399).contains(it.responseCode.value()))
      }
    }
  }

  fun checkHost(ctx: Context, isHttps: Boolean, canonicalHost: String?) =
    if (canonicalHost != null && (ctx.host != canonicalHost || ctx.isHttps != isHttps))
      ctx.sendRedirect("http${if (isHttps) "s" else ""}://$canonicalHost${ctx.requestPath}${ctx.queryString()}")
    else null
}

class TransactionCoroutineContext(ctx: Context): ThreadContextElement<Transaction?>, AbstractCoroutineContextElement(Key) {
  private val tx = ctx.attribute<Transaction?>("tx")
  companion object Key: CoroutineContext.Key<TransactionCoroutineContext>

  override fun updateThreadContext(context: CoroutineContext) = Transaction.current().also { setCurrent(tx) }
  override fun restoreThreadContext(context: CoroutineContext, oldState: Transaction?) = setCurrent(oldState)

  private fun setCurrent(tx: Transaction?) = Transaction.threadContext.set(tx)
}
