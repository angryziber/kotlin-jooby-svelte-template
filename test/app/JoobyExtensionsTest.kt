package app

import io.jooby.Context
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JoobyExtensionsTest {
  val ctx = mockk<Context>(relaxed = true)

  @Test
  fun isHttps() {
    assertThat(ctx.isHttps).isEqualTo(false)

    every { ctx.scheme } returns "https"
    assertThat(ctx.isHttps).isEqualTo(true)
  }

  @Test
  fun baseUrl() {
    every { ctx.getRequestURL(any()) } answers { "https://host" + it.invocation.args[0] }
    assertThat(ctx.baseUrl).isEqualTo("https://host/${Lang.lang(ctx)}/app")
  }
}
