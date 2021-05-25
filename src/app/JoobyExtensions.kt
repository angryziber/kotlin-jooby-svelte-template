package app

import auth.User
import com.fasterxml.jackson.databind.ObjectMapper
import io.jooby.Context
import io.jooby.Environment
import io.jooby.exception.UnauthorizedException
import io.jooby.require
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong

val Environment.isDev get() = isActive("dev")
val Environment.isTest get() = isActive("test")
val Environment.isHttps get() = isActive("https")

val Context.user: User get() = getUser() ?: throw UnauthorizedException()
fun Context.userJson(): String? = require<ObjectMapper>().writeValueAsString(getUser())

val Context.isHttps get() = scheme == "https"
val Context.baseUrl get() = getRequestURL("/${Lang.lang(this)}/app")

val Context.requestId: String get() = header("X-Request-Id").valueOrNull() ?:
  attributes["requestId"] as String? ?: requestIdGenerator.generate().also { attribute("requestId", it) }

private val requestIdGenerator = object {
  val prefix = (0xFFFF * Math.random()).toInt().toString(16)
  val counter = AtomicLong()
  fun generate() = "$prefix-${counter.incrementAndGet()}"
}

object RequestScope: CoroutineScope {
  private val exceptionHandler = CoroutineExceptionHandler { _, x -> LoggerFactory.getLogger("error").error("Async operation failed", x) }
  override val coroutineContext get() = exceptionHandler + MDCContext()

  fun async(block: suspend CoroutineScope.() -> Unit) = launch(start = UNDISPATCHED, block = block)
}
