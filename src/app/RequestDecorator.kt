package app

import db.withTransaction
import io.jooby.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import auth.User
import java.util.concurrent.atomic.AtomicLong
import javax.sql.DataSource

class RequestDecorator(
  private val requestLog: Logger = LoggerFactory.getLogger("request")
): Extension {
  private val canonicalHost = System.getenv("CANONICAL_HOST")
  private val requestId = object {
    val prefix = (0xFFFF * Math.random()).toInt().toString(16)
    val counter = AtomicLong()
    fun generate() = "$prefix-${counter.incrementAndGet()}"
  }

  override fun install(app: Jooby): Unit = (app as Kooby).run {
    val db = require<DataSource>()
    decorator {
      val proxyHeaders = ProxyHeaders(ctx)
      runWithLogging(ctx, proxyHeaders) {
        db.withTransaction {
          checkHost(ctx, proxyHeaders, app.environment.isHttps, canonicalHost) ?: next.apply(ctx)
        }
      }
    }
  }

  fun checkHost(ctx: Context, proxyHeaders: ProxyHeaders, isHttps: Boolean, canonicalHost: String?) =
    if (canonicalHost != null && (ctx.header("Host").value() != canonicalHost || proxyHeaders.isHttps != isHttps))
      ctx.sendRedirect("http${if (isHttps) "s" else ""}://$canonicalHost${ctx.requestPath}${ctx.queryString()}")
    else null

  fun runWithLogging(ctx: Context, proxyHeaders: ProxyHeaders, block: () -> Any): Any {
    if (ctx.route.handler is AssetHandler) return block()
    val start = System.nanoTime()
    val requestId = proxyHeaders.requestId ?: requestId.generate()
    ctx.onComplete {
      val millis = (System.nanoTime() - start) / 1_000_000
      val statusCode = ctx.responseCode.value()
      val user = ctx.getUser<User>()?.run { "$role:$id" } ?: "?"
      val ip = proxyHeaders.ip ?: ctx.remoteAddress
      val referrer = ctx.header("Referer").valueOrNull() ?: ""
      val userAgent = ctx.header("User-Agent").valueOrNull() ?: ""
      val path = ctx.path("userIdHash").valueOrNull()?.let { ctx.requestPath.replace(it, "***") } ?: ctx.requestPath
      runWith(requestId) {
        // Note: log format is parsed in .goaccessrc and EventLogRepository
        requestLog.info("""$user $ip "${ctx.method} ${path}${ctx.queryString()}" $statusCode ${ctx.responseLength} $millis ms $referrer "$userAgent"""")
      }
    }
    return runWith(requestId, block)
  }

  private fun runWith(requestId: String, block: () -> Any) = try {
    MDC.put("requestId", requestId)
    block()
  }
  finally {
    MDC.remove("requestId")
  }
}

data class ProxyHeaders(val requestId: String?, val ip: String?, val isHttps: Boolean) {
  constructor(ctx: Context): this(
    ctx.header("X-Request-Id").valueOrNull(),
    ctx.header("X-Forwarded-For").valueOrNull(),
    ctx.header("X-Forwarded-Proto").valueOrNull() == "https"
  )
}
