package app

import auth.*
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mitchellbosecke.pebble.loader.ClasspathLoader
import com.mitchellbosecke.pebble.loader.FileLoader
import db.DBModule
import io.jooby.*
import io.jooby.json.JacksonModule
import io.jooby.pebble.PebbleModule
import org.slf4j.LoggerFactory.getLogger
import java.io.File

val objectMapper = JacksonModule.create().disable(WRITE_DATES_AS_TIMESTAMPS).setSerializationInclusion(NON_NULL).configure(FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(KotlinModule())

class App: Kooby({
  serverOptions {
    port = System.getenv("PORT")?.toInt() ?: 8080
    isTrustProxy = true
  }
  decorator(HeadHandler())
  registry(AutoCreatingServiceRegistry(services))
  install(DBModule())
  install(JacksonModule(objectMapper))
  install(RequestLogger())
  install(ExceptionHandler())
  install(RequestTransactionHandler())

  registerDependencies()

  val apiVersion = System.getenv("API_VERSION") ?: "%API_VERSION%"
  before { ctx.setResponseHeader("x-api-version", apiVersion) }

  install(AuthModule())
  registerRoutes()

  val assetsDir = File("build/public").takeIf { it.exists() } ?: File("public")

  assets("/*", assetsDir.toPath())
  handleStaticPages(assetsDir, apiVersion)

  get("/api/health") { "OK" }.accessPublic

  post("/api/js-error") {
    getLogger("js-error").error(ctx.body().value())
  }.accessPublic

  post("/api/csp-report") {
    getLogger("csp-report").warn(ctx.body().value())
  }.accessPublic
})
