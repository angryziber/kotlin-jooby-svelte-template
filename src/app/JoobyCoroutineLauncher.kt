package io.jooby.internal.mvc

import app.TransactionCoroutineContext
import io.jooby.Context
import io.jooby.CoroutineRouter
import io.jooby.Route
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * This class is copied from Jooby to extend CouroutineContext when launching new coroutines.
 * TODO: monitor this for a better way to do it in future: https://github.com/jooby-project/jooby/issues/2257
 */
class CoroutineLauncher(val next: Route.Handler): Route.Handler {
  override fun apply(ctx: Context): Any {
    val router = ctx.router.attribute<CoroutineRouter>("coroutineRouter")
    val coroutineContext = CoroutineExceptionHandler { _, x -> ctx.sendError(x) } + MDCContext() + TransactionCoroutineContext(ctx)
    router.coroutineScope.launch(coroutineContext, router.coroutineStart) {
      val result = suspendCoroutineUninterceptedOrReturn<Any> {
        ctx.attribute("___continuation", it)
        next.apply(ctx)
      }
      if (!ctx.isResponseStarted) ctx.render(result)
    }
    return ctx
  }
}
