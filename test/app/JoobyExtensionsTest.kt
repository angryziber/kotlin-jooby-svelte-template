package app

import io.jooby.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

  @Test
  fun `requestId from load balancer eg Heroku`() {
    every { ctx.header("X-Request-Id").valueOrNull() } returns "r-id-123"
    assertThat(ctx.requestId).isEqualTo("r-id-123")
  }

  @Test
  fun `generated requestId`() {
    every { ctx.header("X-Request-Id").valueOrNull() } returns null
    every { ctx.attributes } returns emptyMap()
    val requestId = ctx.requestId
    assertThat(requestId).matches(".+-1")
    verify { ctx.attribute("requestId", requestId) }
  }

  @Test
  fun `remembered requestId`() {
    every { ctx.header("X-Request-Id").valueOrNull() } returns null
    every { ctx.attributes } returns mapOf("requestId" to "remembered-id")
    assertThat(ctx.requestId).isEqualTo("remembered-id")
  }
}
