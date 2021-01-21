package app

import auth.AuthController
import auth.AuthModule
import auth.FakeLoginForTestingController
import auth.User
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mitchellbosecke.pebble.loader.ClasspathLoader
import com.mitchellbosecke.pebble.loader.FileLoader
import com.typesafe.config.Config
import db.DBModule
import io.jooby.*
import io.jooby.StatusCode.MOVED_PERMANENTLY
import io.jooby.json.JacksonModule
import io.jooby.pebble.PebbleModule
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
  install(JacksonModule(JacksonModule.create().disable(WRITE_DATES_AS_TIMESTAMPS).setSerializationInclusion(NON_NULL).configure(FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(KotlinModule())))
  val pebbleLoader = if (environment.isDev) FileLoader().apply { prefix = "ui/static" } else ClasspathLoader()
  install(PebbleModule(PebbleModule.create().setTemplateLoader(pebbleLoader).build(environment)))
  install(RequestLogger())
  install(ExceptionHandler())
  install(RequestTransactionHandler())
  install(AuthModule())

  val apiVersion = System.getenv("API_VERSION") ?: "%API_VERSION%"
  val uiConfigJson = require<ObjectMapper>().writeValueAsString(mapOf("apiVersion" to apiVersion))

  before {
    ctx.setResponseHeader("x-api-version", apiVersion)
  }

  registerServicesAndControllers()

  val assetsDir = File("build/public").takeIf { it.exists() } ?: File("public")
  val assetsTime = assetsDir.lastModified()

  val devRollupLivereload = if (environment.isDev) "http://localhost:35729 ws://localhost:35729" else ""
  val csp = "default-src 'self' 'unsafe-inline' $devRollupLivereload ${config.getString("csp.allowedExternalSrc")}; " +
            "img-src 'self' data:; " +
            "report-uri /csp-report"

  assets("/*", assetsDir.toPath())

  get("/") {
    ctx.sendRedirect("/${Lang.detect(ctx)}/${ctx.initialPage()}/")
  }

  get("/{lang:[a-z]{2}}") {
    ctx.sendRedirect("/${ctx.path("lang")}/${ctx.initialPage()}/")
  }

  get("/{lang:[a-z]{2}}/{page}") {
    ctx.sendRedirect(ctx.requestPath + "/")
  }

  get("/{lang:[a-z]{2}}/{page}/*") {
    val lang = ctx.path("lang").value()
    val page = ctx.path("page").value()
    val translations = Lang.translations[lang] ?:
      return@get ctx.sendRedirect(MOVED_PERMANENTLY, ctx.requestPath.replace("/$lang/", "/${Lang.lang(ctx)}/"))
    Lang.remember(ctx, lang)
    ctx.setResponseHeader("Content-Security-Policy", csp)
    ctx.setResponseHeader("X-Frame-Options", "SAMEORIGIN")
    if (environment.isHttps) ctx.setResponseHeader("Strict-Transport-Security", "max-age=31536000")

    ModelAndView("pages/$page.peb", translations).put("assetsTime", assetsTime)
      .put("lang", lang).put("langs", Lang.available).put("envs", environment.activeNames)
      .put("globalWarning", ctx.warnLegacyBrowsers(translations))
      .put("configJson", uiConfigJson)
  }

  post("/js-error") {
    getLogger("js-error").error(ctx.body().value())
  }

  post("/csp-report") {
    getLogger("csp-report").warn(ctx.body().value())
  }
})

private fun Context.initialPage() = if (getUser<User>() == null) "home" else "app"

private fun Kooby.registerServicesAndControllers() {
  services.put(Environment::class.java, environment)
  services.put(Config::class.java, config)

  mvc<AuthController>()
  if (environment.isActive("test")) mvc<FakeLoginForTestingController>()
}

fun Context.warnLegacyBrowsers(translate: Translations): String? {
  val userAgent = header("User-Agent").value("")
  return when {
    userAgent.contains("Edge/") -> translate("errors.upgradeLegacyEdgeBrowser")
    userAgent.contains("MSIE") || userAgent.contains("Trident") -> translate("errors.unsupportedIEBrowser")
    else -> null
  }
}
