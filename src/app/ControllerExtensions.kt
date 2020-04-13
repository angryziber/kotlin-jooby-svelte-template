package app

import io.jooby.Context
import io.jooby.Jooby
import io.jooby.require

interface Before {
  fun before(ctx: Context)
}

inline fun <reified T: Any> Jooby.mvc() {
  val controller = require<T>()
  if (controller is Before) before(controller::before)
  mvc(controller)
}
