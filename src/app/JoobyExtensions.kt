package app

import auth.User
import com.fasterxml.jackson.databind.ObjectMapper
import io.jooby.Context
import io.jooby.Environment
import io.jooby.exception.UnauthorizedException
import io.jooby.require
import java.util.concurrent.atomic.AtomicLong

val Environment.isDev get() = isActive("dev")
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
