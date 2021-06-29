package app

import auth.User
import com.fasterxml.jackson.databind.ObjectMapper
import io.jooby.*
import java.io.File

fun Kooby.handleStaticPages(assetsDir: File, apiVersion: String) {
  val assetsTime = assetsDir.lastModified()
  val uiConfigJson = require<ObjectMapper>().writeValueAsString(mapOf("apiVersion" to apiVersion))

  val csp = "default-src 'self' 'unsafe-inline' ${config.getString("csp.allowedExternalSrc")}; " +
    "img-src 'self' data:; " +
    "report-uri /api/csp-report"

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
    val translations = Lang.translations[lang]?.let { Lang.translations.values.first() + it } ?:
    return@get ctx.sendRedirect(StatusCode.MOVED_PERMANENTLY, ctx.requestPath.replace("/$lang/", "/${Lang.lang(ctx)}/"))
    Lang.remember(ctx, lang)
    ctx.setResponseHeader("Content-Security-Policy", csp)
    ctx.setResponseHeader("X-Frame-Options", "SAMEORIGIN")
    if (environment.isHttps) ctx.setResponseHeader("Strict-Transport-Security", "max-age=31536000")

    ModelAndView("pages/$page.peb", translations).put("assetsTime", assetsTime)
      .put("lang", lang).put("langs", Lang.available).put("envs", environment.activeNames)
      .put("globalWarning", ctx.warnLegacyBrowsers(translations))
      .put("configJson", uiConfigJson)
  }
}

private fun Context.initialPage() = if (getUser<User>() == null) "home" else "app"

private fun Context.warnLegacyBrowsers(translate: Translations): String? {
  val userAgent = header("User-Agent").value("")
  return when {
    userAgent.contains("Edge/") -> translate("errors.upgradeLegacyEdgeBrowser")
    userAgent.contains("MSIE") || userAgent.contains("Trident") -> translate("errors.unsupportedIEBrowser")
    else -> null
  }
}
