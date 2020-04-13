package ui

import app.invoke
import com.codeborne.selenide.Condition.visible
import com.codeborne.selenide.Selectors.byName
import com.codeborne.selenide.Selectors.byText
import com.codeborne.selenide.Selenide.clearBrowserCookies
import com.codeborne.selenide.Selenide.open
import com.codeborne.selenide.WebDriverRunner.url
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class LoginUITest: UITest() {
  @Test
  fun `navigate from home to login`() {
    clearBrowserCookies()
    open("/")
    elByText("login.submit").click()
    elByText("login.title").shouldBe(visible)
    assertThat(url()).endsWith("/en/app/login")
  }

  @Test
  fun superAdmin() {
    loginWithPassword("admin")
    elByText("admin.dashboard.title").shouldBe(visible)
    assertThat(url()).endsWith("/en/app/admin")
  }

  private fun loginWithPassword(login: String) {
    open("/en/app/login")
    el(byName("login")).value = login
    el(byName("password")).setValue("secret").pressEnter()
  }

  @AfterEach
  fun logout() {
    val logoutLink = el(byText(translate("logout.linkTitle")))
    if (logoutLink.exists()) {
      logoutLink.click()
      assertThat(url()).endsWith("/en/home/")
    }
  }
}
