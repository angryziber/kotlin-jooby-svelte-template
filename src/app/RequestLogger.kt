package app

import auth.User
import io.jooby.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

class RequestLogger(private val requestLog: Logger = LoggerFactory.getLogger("request")): Extension {
  override fun install(app: Jooby): Unit = (app as Kooby).run {
    before { ctx ->
      if (ctx.route.handler !is AssetHandler) logOnComplete(ctx) else MDC.clear()
    }
  }

  fun logOnComplete(ctx: Context) {
    val start = System.nanoTime()
    val requestId = ctx.requestId
    MDC.put("requestId", requestId)
    ctx.onComplete {
      val millis = (System.nanoTime() - start) / 1_000_000
      val statusCode = ctx.responseCode.value()
      val user = ctx.getUser<User>()
      val userInfo = user?.run { "$role:$id" } ?: "?"
      val referrer = ctx.header("Referer").value("")
      val userAgent = ctx.header("User-Agent").value("")
      val path = ctx.requestPath
      val url = "${path}${ctx.queryString()}"
      MDC.put("requestId", requestId)
      requestLog.info("""$userInfo "${ctx.method} $url" $statusCode ${ctx.responseLength} $millis ms $referrer "$userAgent"""")
      MDC.remove("requestId")
    }
  }
}
