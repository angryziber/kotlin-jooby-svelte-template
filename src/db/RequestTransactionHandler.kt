package db

import io.jooby.*
import javax.sql.DataSource

class RequestTransactionHandler: Extension {
  override fun install(app: Jooby): Unit = (app as Kooby).run {
    val db = require<DataSource>()

    before {
      val tx = Transaction(db).attachToRequest()
      ctx.onComplete {
        // close tx for non-blocking (coroutine) requests
        tx.close(commit = (200..399).contains(it.responseCode.value()))
      }
    }

    after {
      if (ctx.isInIoThread) {
        // Jooby shortcoming: "after" runs before coroutine handlers (https://github.com/jooby-project/jooby/issues/2465)
        // so we just detach and will end the transaction in "onComplete" above
        Transaction.current()!!.detachFromRequest()
      }
      else Transaction.current()!!.close(commit = failure == null)
      Unit
    }
  }
}
