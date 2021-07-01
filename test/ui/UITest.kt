package ui
import app.App
import app.Lang
import app.invoke
import app.objectMapper
import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selectors
import com.codeborne.selenide.Selectors.byText
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.open
import com.codeborne.selenide.SelenideElement
import com.fasterxml.jackson.databind.ObjectMapper
import io.jooby.ServerOptions
import org.intellij.lang.annotations.Language
import org.openqa.selenium.By
import util.stringify

/**
 * Base class for UI (E2E) tests written using Selenide
 */
abstract class UITest {
  companion object {
    init {
      System.setProperty("application.env", "test,test-data")
    }

    protected val app = App().apply {
      serverOptions = ServerOptions().apply { port = 18097 }
    }

    init {
      app.start()
      Configuration.baseUrl = "http://localhost:18097"
    }
  }

  val translate = Lang.translations["en"]!!

  fun fakeLogin(login: String, page: String? = null) {
    open("/fake-login/$login" + (page?.let { "?page=$page" } ?: ""))
  }

  @Language("JavaScript")
  fun modifySession(map: Map<String, Any?>) = Selenide.executeAsyncJavaScript<Any>("""
    let callback = arguments[0]
    fetch('/fake-login/session', {method: 'POST', body: '${objectMapper.stringify(map)}', headers: {'Content-Type': 'application/json'}}).then(callback)
  """)

  fun el(selector: String) = Selenide.`$`(selector)
  fun els(selector: String) = Selenide.`$$`(selector)
  fun el(selector: String, i: Int) = Selenide.`$`(selector, i)
  fun el(selector: By) = Selenide.`$`(selector)
  fun el(selector: By, i: Int) = Selenide.`$`(selector, i)
  fun elByText(key: String) = el(byText(translate(key)))
  fun elByText(key: String, i: Int) = el(byText(translate(key)), i)
  fun elByText(key: String, replacements: Map<String, String>) =
    el(byText(replacements.entries.fold(translate(key)) { text, e ->
      text.replace("{${e.key}}", e.value)
    }))

  fun SelenideElement.byText(key: String) = find(Selectors.byText(translate(key)))
}
