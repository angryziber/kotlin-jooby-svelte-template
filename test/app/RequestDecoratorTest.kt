package app

import auth.User
import com.zaxxer.hikari.util.DriverDataSource
import db.withConnection
import io.jooby.*
import io.jooby.StatusCode.ACCEPTED
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.MDC
import javax.sql.DataSource

class RequestDecoratorTest {
  val ctx = mockk<Context>(relaxed = true) {
    every { remoteAddress } returns "127.0.0.13"
    every { method } returns "GET"
    every { requestPath } returns "/path"
    every { attributes } returns emptyMap()
    every { queryString() } returns "?q=hello"
    every { path("userIdHash").valueOrNull() } returns null
    every { header(any()).valueOrNull() } returns null
    every { header("Referer").value("") } returns "http://referrer"
    every { header("User-Agent").value("") } returns "User-Agent"
    every { responseLength } returns 12345
    every { responseCode } returns ACCEPTED
    every { getUser<User>() } returns TestData.user
  }

  val requestLog = mockk<Logger>(relaxed = true)
  val handler = RequestDecorator(requestLog)

  @Test
  fun `decorator starts transaction and preserves return value`() {
    val decorator = slot<DecoratorContext.() -> Any>()
    val app = mockk<Kooby>(relaxed = true)
    val db = mockk<DriverDataSource>(relaxed = true)
    every { app.require<DataSource>() } returns db
    handler.install(app)
    verify { app.decorator(capture(decorator)) }

    val next = mockk<Route.Handler> {
      every { apply(ctx) } answers {
        db.withConnection { }
        db.withConnection { }
        "Result"
      }
    }
    assertThat(decorator.captured.invoke(DecoratorContext(ctx, next))).isEqualTo("Result")
    verify(exactly = 1) { db.connection }
  }

  @Test
  fun `canonical host is enforced`() {
    assertThat(handler.checkHost(ctx,  true, "app.ee")).isInstanceOf(Context::class.java)
    verify { ctx.sendRedirect("https://app.ee/path?q=hello") }
  }

  @Test
  fun `successful request log without proxy`() {
    every { ctx.requestId } returns "r-id"
    handler.runWithLogging(ctx) {
      assertThat(MDC.get("requestId")).endsWith("r-id")
    }
    runCompleteHandler()

    verify { requestLog.info(match { it.matches(
      """USER:${TestData.user.id} 127.0.0.13 "GET /path\?q=hello" 202 12345 \d+ ms http://referrer "User-Agent"""".toRegex()
    )})}
    assertThat(MDC.get("requestId")).isNull()
  }

  private fun runCompleteHandler() {
    val completeHandler = slot<Route.Complete>()
    verify { ctx.onComplete(capture(completeHandler)) }
    completeHandler.captured.apply(ctx)
  }
}
