package app

import com.zaxxer.hikari.util.DriverDataSource
import db.Transaction
import io.jooby.*
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.sql.Connection
import javax.sql.DataSource

class RequestTransactionHandlerTest {
  val ctx = mockk<Context>(relaxed = true) {
    every { requestPath } returns "/path"
    every { queryString() } returns "?q=hello"
  }
  val conn = mockk<Connection>(relaxed = true)
  val db = mockk<DriverDataSource>(relaxed = true) {
    every { connection } returns conn
  }
  val handler = RequestTransactionHandler()

  @Test
  fun `commit on success`() {
    val (before, after) = checkInstall()

    before.captured.invoke(HandlerContext(ctx))
    assertThat(Transaction.current()!!.connection).isSameAs(conn)

    after.captured.invoke(AfterContext(ctx, "Result", failure = null))
    verify {
      conn.commit()
      conn.close()
    }
    assertThat(Transaction.current()).isNull()
  }

  @Test
  fun `rollback on failure`() {
    val (before, after) = checkInstall()

    before.captured.invoke(HandlerContext(ctx))
    assertThat(Transaction.current()!!.connection).isSameAs(conn)

    after.captured.invoke(AfterContext(ctx, "Result", failure = Exception("Failure")))
    verify {
      conn.rollback()
      conn.close()
    }
    assertThat(Transaction.current()).isNull()
  }

  private fun checkInstall(): Pair<CapturingSlot<HandlerContext.() -> Unit>, CapturingSlot<AfterContext.() -> Any>> {
    val before = slot<HandlerContext.() -> Unit>()
    val after = slot<AfterContext.() -> Any>()
    val app = mockk<Kooby>(relaxed = true)
    every { app.require<DataSource>() } returns db

    handler.install(app)

    verify { app.before(capture(before)) }
    verify { app.after(capture(after)) }
    return Pair(before, after)
  }

  @Test
  fun `canonical host is enforced`() {
    assertThat(handler.checkHost(ctx, true, "www.anonima.net")).isInstanceOf(Context::class.java)
    verify { ctx.sendRedirect("https://www.anonima.net/path?q=hello") }
  }
}
