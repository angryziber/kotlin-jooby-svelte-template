package jobs

import app.RequestLogger
import db.Transaction
import db.TransactionCoroutineContext
import io.jooby.Extension
import io.jooby.Jooby
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.slf4j.MDCContext
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicLong
import javax.sql.DataSource

class JobRunner(private val db: DataSource): Extension, CoroutineScope {
  private val log = LoggerFactory.getLogger(javaClass)
  private val executor = Executors.newScheduledThreadPool(3)
  override val coroutineContext = executor.asCoroutineDispatcher()
  private val seq = AtomicLong()
  private val runningJobs = mutableSetOf<kotlinx.coroutines.Job>()

  override fun install(app: Jooby) {
    app.onStop(::gracefulStop)
  }

  fun schedule(job: Job, delay: Long, period: Long, unit: TimeUnit) {
    val jobName = job::class.simpleName
    val requestId = "${RequestLogger.prefix}/$jobName#${seq.incrementAndGet()}"
    log.info("$jobName will start after $delay $unit and run every $period $unit")
    executor.scheduleAtFixedRate({
      val tx = Transaction(db)
      val launched = launch(start = UNDISPATCHED, context = MDCContext(mapOf("requestId" to requestId)) + TransactionCoroutineContext(tx)) {
        try {
          job.run()
          tx.close(true)
        } catch (e: Exception) {
          log.error("$jobName failed", e)
          tx.close(false)
        }
      }
      runningJobs += launched
      launched.invokeOnCompletion { runningJobs -= launched }
    }, delay, period, unit)
  }

  fun scheduleDaily(job: Job) = schedule(job, (Math.random() * 10).toLong(), 24 * 60, MINUTES)

  private fun gracefulStop() {
    runBlocking {
      runningJobs.forEach { it.cancelAndJoin() }
    }
    executor.shutdown()
    executor.awaitTermination(10, SECONDS)
  }
}

fun interface Job {
  suspend fun run()
}
