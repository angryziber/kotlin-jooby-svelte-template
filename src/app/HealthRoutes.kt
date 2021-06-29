package app

import auth.Access
import auth.Role.PUBLIC
import io.jooby.annotations.GET
import io.jooby.annotations.Path
import java.time.Instant

@Path("/api/health")
class HealthRoutes {
  @GET @Access(PUBLIC)
  fun get() = HealthResponse()

  class HealthResponse {
    companion object {
      private val startTime = Instant.now()
      private val runtime = Runtime.getRuntime()
    }
    val status = "up"
    val version = System.getenv("VERSION")
    val startedAt = startTime
    val node = RequestLogger.prefix
    val totalMemoryMib =  runtime.totalMemory() / 1024 / 1024
    val usedMemoryMib = totalMemoryMib - runtime.freeMemory() / 1024 / 1024
  }
}
