package app

import com.fasterxml.jackson.databind.ObjectMapper
import io.jooby.Context
import io.jooby.Cookie
import java.io.InputStreamReader

typealias Translations = Map<String, Any>

object Lang {
  const val COOKIE = "LANG"

  val available = readAvailableLangs("/langs.ts")
  val translations = available.associateWith { lang -> load(lang) }

  fun detect(ctx: Context) = lang(ctx).also { remember(ctx, it) }

  fun lang(ctx: Context): String {
    val requestedLang = ctx.cookie(COOKIE).valueOrNull() ?: acceptLanguage(ctx.header("Accept-Language").valueOrNull())
    return if (available.contains(requestedLang)) requestedLang!! else available.first()
  }

  fun remember(ctx: Context, lang: String) {
    ctx.setResponseCookie(Cookie(COOKIE, lang))
  }

  fun translations(ctx: Context): Translations = translations[lang(ctx)]!!

  private fun acceptLanguage(accept: String?) = accept?.replace("[-,;].*$".toRegex(), "")

  private fun readAvailableLangs(jsFile: String) = InputStreamReader(javaClass.getResourceAsStream(jsFile)).readLines()
    .filter { it.startsWith("import") }.map { it.split(" ")[1] }

  @Suppress("UNCHECKED_CAST")
  private fun load(lang: String) =
    ObjectMapper().readValue(javaClass.getResource("/$lang.json"), MutableMap::class.java) as Translations
}

private fun Translations.resolve(key: String) =
  key.split('.').fold(this) { more: Any?, k -> (more as? Map<*, *>)?.get(k) }

@Suppress("UNCHECKED_CAST")
fun Translations.getMany(key: String) = resolve(key) as? Map<String, String> ?: emptyMap()
operator fun Translations.invoke(key: String) = resolve(key) as? String ?: key
