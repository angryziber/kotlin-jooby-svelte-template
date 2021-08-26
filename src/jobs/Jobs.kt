package jobs

import io.jooby.Kooby
import io.jooby.require
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

fun Kooby.startJobs() = require<JobRunner>().apply {
  install(this)
  scheduleDaily {
    withContext(NonCancellable) {
      println("Dummy Job started")
      delay(3000)
      println("Dummy Job finished")
    }
  }
}
