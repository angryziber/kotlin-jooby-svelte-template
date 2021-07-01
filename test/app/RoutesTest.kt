package app

import io.jooby.Context
import io.jooby.Jooby
import io.jooby.require
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class RoutesTest {
  val app = mockk<Jooby>(relaxed = true)

  @Test
  fun `mvc routes without before handler`() {
    val controller = RegularRoutes()
    every { app.require<RegularRoutes>() } returns controller
    app.mvc<RegularRoutes>()
    verify { app.mvc(controller) }
  }

  @Test
  fun `mvc routes with before handler`() {
    val controller = WithBeforeRoutes()
    every { app.require<WithBeforeRoutes>() } returns controller
    app.mvc<WithBeforeRoutes>()
    verify {
      app.before(any())
      app.mvc(controller)
    }
  }

  class RegularRoutes
  class WithBeforeRoutes: Before {
    override fun before(ctx: Context) {}
  }
}
