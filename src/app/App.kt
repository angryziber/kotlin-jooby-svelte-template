package app

import auth.AuthModule
import auth.accessPublic
import db.DBModule
import db.RequestTransactionHandler
import io.jooby.HeadHandler
import io.jooby.Kooby
import org.slf4j.LoggerFactory.getLogger
import java.io.File

class App: Kooby({
  serverOptions {
    port = System.getenv("PORT")?.toInt() ?: 8080
    isTrustProxy = true
  }
  decorator(HeadHandler())
  registry(AutoCreatingServiceRegistry(services))
  install(DBModule())
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

  post("/api/js-error") {
    getLogger("js-error").error(ctx.body().value())
  }.accessPublic

  post("/api/csp-report") {
    getLogger("csp-report").warn(ctx.body().value())
  }.accessPublic
})
