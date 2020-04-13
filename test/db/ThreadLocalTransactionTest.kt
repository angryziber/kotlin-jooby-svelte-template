package db

import com.zaxxer.hikari.util.DriverDataSource
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ThreadLocalTransactionTest {
  val db = mockk<DriverDataSource>(relaxed = true)

  @Test
  fun `transaction does not open connection at start`() {
    db.withTransaction {
      // no db access here
    }
    verify(exactly = 0) { db.connection }
  }

  @Test
  fun `transaction creates and reuses connection on demand`() {
    db.withTransaction {
      val conn = db.withConnection { this }
      assertThat(db.withConnection { this }).isSameAs(conn)
      verify { conn.autoCommit = false }
    }
    verify(exactly = 1) {
      db.connection.apply {
        commit()
        autoCommit = true
        close()
      }
    }
  }

  @Test
  fun `transaction with rollbackOnly rolls back`() {
    db.withTransaction(rollbackOnly = true) {
      val conn = db.withConnection { this }
      verify { conn.autoCommit = false }
    }
    verify(exactly = 1) {
      db.connection.apply {
        rollback()
        autoCommit = true
        close()
      }
    }
  }

  @Test
  fun `transaction rolls back on exception`() {
    assertThrows<RuntimeException> {
      db.withTransaction {
        db.withConnection { throw RuntimeException() }
      }
    }
    verify(exactly = 1) {
      db.connection.apply {
        autoCommit = false
        rollback()
        autoCommit = true
        close()
      }
    }
  }

  @Test
  fun `connection without transaction is closed`() {
    val conn = db.withConnection { this }
    verify { conn.close() }
    verify(exactly = 0) { conn.autoCommit = any() }
  }
}
