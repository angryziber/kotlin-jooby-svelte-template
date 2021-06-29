package app

import auth.AuthController
import auth.FakeLoginForTestingController
import io.jooby.Kooby
import kotlinx.coroutines.slf4j.MDCContext

fun Kooby.registerRoutes() {
  coroutine {
    launchContext { it + MDCContext() + TransactionCoroutineContext() }

    mvc<AuthController>()
    if (environment.isTest) mvc<FakeLoginForTestingController>()
  }
}
