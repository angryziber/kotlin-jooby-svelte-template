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
      val tx = Transaction(db).attachToRequest()
      ctx.onComplete {
        tx.close(commit = (200..399).contains(it.responseCode.value()))
      }
    }

    after {
      Transaction.current()?.detachFromRequest() ?: Unit
    }
  }

  fun checkHost(ctx: Context, isHttps: Boolean, canonicalHost: String?) =
    if (canonicalHost != null && (ctx.host != canonicalHost || ctx.isHttps != isHttps))
      ctx.sendRedirect("http${if (isHttps) "s" else ""}://$canonicalHost${ctx.requestPath}${ctx.queryString()}")
    else null
}

class TransactionCoroutineContext: ThreadContextElement<Transaction?>, AbstractCoroutineContextElement(Key) {
  private val tx = Transaction.current()
  companion object Key: CoroutineContext.Key<TransactionCoroutineContext>

  override fun updateThreadContext(context: CoroutineContext) = tx?.attachToRequest()
  override fun restoreThreadContext(context: CoroutineContext, oldState: Transaction?) { oldState?.attachToRequest() }
}
