package app

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
import auth.User
import javax.sql.DataSource

class RequestDecoratorTest {
  val ctx = mockk<Context>(relaxed = true) {
    every { remoteAddress } returns "127.0.0.13"
    every { method } returns "GET"
    every { requestPath } returns "/path"
    every { queryString() } returns "?q=hello"
    every { path("userIdHash").valueOrNull() } returns null
    every { header(any()).valueOrNull() } returns null
    every { header("Referer").valueOrNull() } returns "http://referrer"
    every { header("User-Agent").valueOrNull() } returns "User-Agent"
    every { responseLength } returns 12345
    every { responseCode } returns ACCEPTED
    every { getUser<User>() } returns TestData.user
  }
  val db = mockk<DriverDataSource>(relaxed = true)

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
    assertThat(handler.checkHost(ctx,  ProxyHeaders(ctx), true, "app.ee")).isInstanceOf(Context::class.java)
    verify { ctx.sendRedirect("https://app.ee/path?q=hello") }
  }

  @Test
  fun `successful request log without proxy`() {
    handler.runWithLogging(ctx, ProxyHeaders(ctx)) {
      assertThat(MDC.get("requestId")).endsWith("-1")
    }
    runCompleteHandler()

    verify { requestLog.info(match { it.matches(
      """USER:${TestData.user.id} 127.0.0.13 "GET /path\?q=hello" 202 12345 \d+ ms http://referrer "User-Agent"""".toRegex()
    )})}
    assertThat(MDC.get("requestId")).isNull()
  }

  @Test
  fun `successful request log with proxy headers`() {
    every { ctx.header("X-Request-Id").valueOrNull() } returns "r-id"
    every { ctx.header("X-Forwarded-For").valueOrNull() } returns "192.168.33.44"

    handler.runWithLogging(ctx, ProxyHeaders(ctx)) {
      assertThat(MDC.get("requestId")).isEqualTo("r-id")
    }

    runCompleteHandler()
    verify { requestLog.info(match { it.contains(" 192.168.33.44 ")})}
    assertThat(MDC.get("requestId")).isNull()
  }

  @Test
  fun `removes userIdHash from path`() {
    every { ctx.requestPath } returns "/api/1234567/endpoint"
    every { ctx.path("userIdHash").valueOrNull() } returns "1234567"
    handler.runWithLogging(ctx, ProxyHeaders(ctx)) {}
    runCompleteHandler()
    verify { requestLog.info(match { it.contains("GET /api/***/endpoint?q=hello")})}
  }

  private fun runCompleteHandler() {
    val completeHandler = slot<Route.Complete>()
    verify { ctx.onComplete(capture(completeHandler)) }
    completeHandler.captured.apply(ctx)
  }
}
