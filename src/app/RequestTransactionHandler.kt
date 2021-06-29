package app

import db.Transaction
import io.jooby.*
import javax.sql.DataSource

class RequestTransactionHandler: Extension {
  override fun install(app: Jooby): Unit = (app as Kooby).run {
    val db = require<DataSource>()

    before {
      val tx = Transaction(db).attachToRequest()
      ctx.onComplete {
        tx.close(commit = (200..399).contains(it.responseCode.value()))
      }
    }

    after {
      Transaction.current()?.detachFromRequest() ?: Unit
    }
  }
}
