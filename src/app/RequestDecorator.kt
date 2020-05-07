package app

import auth.User
import db.withTransaction
import io.jooby.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import javax.sql.DataSource

class RequestDecorator(
  private val requestLog: Logger = LoggerFactory.getLogger("request")
): Extension {
  private val canonicalHost = System.getenv("CANONICAL_HOST")

  override fun install(app: Jooby): Unit = (app as Kooby).run {
    val db = require<DataSource>()
    decorator {
      runWithLogging(ctx) {
        db.withTransaction {
          checkHost(ctx, environment.isHttps, canonicalHost) ?: next.apply(ctx)
        }
      }
    }
  }

  fun checkHost(ctx: Context, isHttps: Boolean, canonicalHost: String?) =
    if (canonicalHost != null && (ctx.host != canonicalHost || ctx.isHttps != isHttps))
      ctx.sendRedirect("http${if (isHttps) "s" else ""}://$canonicalHost${ctx.requestPath}${ctx.queryString()}")
    else null

  fun runWithLogging(ctx: Context, block: () -> Any): Any {
    if (ctx.route.handler is AssetHandler) return block()
    val start = System.nanoTime()
    ctx.onComplete {
      val millis = (System.nanoTime() - start) / 1_000_000
      val statusCode = ctx.responseCode.value()
      val user = ctx.getUser<User>()?.run { "$role:$id" } ?: "?"
      val referrer = ctx.header("Referer").value("")
      val userAgent = ctx.header("User-Agent").value("")
      runWith(ctx.requestId) {
        requestLog.info("""$user ${ctx.remoteAddress} "${ctx.method} ${ctx.requestPath}${ctx.queryString()}" $statusCode ${ctx.responseLength} $millis ms $referrer "$userAgent"""")
      }
    }
    return runWith(ctx.requestId, block)
  }

  private fun runWith(requestId: String, block: () -> Any) = try {
    MDC.put("requestId", requestId)
    block()
  }
  finally {
    MDC.remove("requestId")
  }
}
