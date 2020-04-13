package app

import io.jooby.Context
import io.jooby.Jooby
import io.jooby.require
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ControllerExtensionsTest {
  val app = mockk<Jooby>(relaxed = true)

  @Test
  fun `mvc controller without before handler`() {
    val controller = RegularController()
    every { app.require<RegularController>() } returns controller
    app.mvc<RegularController>()
    verify { app.mvc(controller) }
  }

  @Test
  fun `mvc controller with before handler`() {
    val controller = WithBeforeController()
    every { app.require<WithBeforeController>() } returns controller
    app.mvc<WithBeforeController>()
    verify {
      app.before(any())
      app.mvc(controller)
    }
  }

  class RegularController
  class WithBeforeController: Before {
    override fun before(ctx: Context) {}
  }
}