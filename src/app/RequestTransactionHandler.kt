package app

import db.Transaction
import io.jooby.*
import javax.sql.DataSource

class RequestTransactionHandler: Extension {
  private val canonicalHost = System.getenv("CANONICAL_HOST")

  override fun install(app: Jooby): Unit = (app as Kooby).run {
    val db = require<DataSource>()

    before {
      checkHost(ctx, environment.isHttps, canonicalHost)
      Transaction(db)
    }

    after {
      Transaction.current()!!.close(commit = failure == null)
    }
  }

  fun checkHost(ctx: Context, isHttps: Boolean, canonicalHost: String?) =
    if (canonicalHost != null && (ctx.host != canonicalHost || ctx.isHttps != isHttps))
      ctx.sendRedirect("http${if (isHttps) "s" else ""}://$canonicalHost${ctx.requestPath}${ctx.queryString()}")
    else null
}
