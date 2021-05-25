package app

import auth.User
import io.jooby.Context
import io.jooby.Environment
import io.jooby.exception.UnauthorizedException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import org.slf4j.LoggerFactory

val Environment.isDev get() = isActive("dev")
val Environment.isTest get() = isActive("test")
val Environment.isHttps get() = isActive("https")

val Context.user: User get() = getUser() ?: throw UnauthorizedException()

val Context.isHttps get() = scheme == "https"
val Context.baseUrl get() = getRequestURL("/${Lang.lang(this)}/app")

object RequestScope: CoroutineScope {
  private val exceptionHandler = CoroutineExceptionHandler { _, x -> LoggerFactory.getLogger("error").error("Async operation failed", x) }
  override val coroutineContext get() = exceptionHandler + MDCContext()

  fun async(block: suspend CoroutineScope.() -> Unit) = launch(start = UNDISPATCHED, block = block)
}
