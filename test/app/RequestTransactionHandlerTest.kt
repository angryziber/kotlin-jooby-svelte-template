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
    every { attribute<Transaction>("tx") } answers { Transaction.current()!! }
  }
  val conn = mockk<Connection>(relaxed = true)
  val db = mockk<DriverDataSource>(relaxed = true) {
    every { connection } returns conn
  }
  val handler = RequestTransactionHandler()

  @Test
  fun `commit on success`() {
    val complete = checkInstall()
    assertThat(Transaction.current()!!.connection).isSameAs(conn)

    every { ctx.responseCode } returns StatusCode.OK
    complete.captured.apply(ctx)

    verify {
      conn.commit()
      conn.close()
    }
    assertThat(Transaction.current()).isNull()
  }

  @Test
  fun `rollback on failure`() {
    val complete = checkInstall()
    assertThat(Transaction.current()!!.connection).isSameAs(conn)

    every { ctx.responseCode } returns StatusCode.SERVER_ERROR
    complete.captured.apply(ctx)
    verify {
      conn.rollback()
      conn.close()
    }
    assertThat(Transaction.current()).isNull()
  }

  private fun checkInstall(): CapturingSlot<Route.Complete> {
    val before = slot<HandlerContext.() -> Unit>()
    val complete = slot<Route.Complete>()
    val app = mockk<Kooby>(relaxed = true)
    every { app.require<DataSource>() } returns db

    handler.install(app)

    verify { app.before(capture(before)) }
    before.captured.invoke(HandlerContext(ctx))

    verify { ctx.onComplete(capture(complete)) }
    return complete
  }

  @Test
  fun `canonical host is enforced`() {
    assertThat(handler.checkHost(ctx, true, "www.anonima.net")).isInstanceOf(Context::class.java)
    verify { ctx.sendRedirect("https://www.anonima.net/path?q=hello") }
  }
}
