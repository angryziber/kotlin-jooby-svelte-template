package app

import io.jooby.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LangTest {
  val ctx = mockk<Context>(relaxed = true) {
    every { cookie(Lang.COOKIE).valueOrNull() } returns null
  }

  @Test
  fun `detect from cookie`() {
    every { ctx.cookie(Lang.COOKIE).valueOrNull() } returns "en"
    assertThat(Lang.detect(ctx)).isEqualTo("en")
  }

  @Test
  fun `detect from cookie not available`() {
    every { ctx.cookie(Lang.COOKIE).valueOrNull() } returns "et"
    assertThat(Lang.detect(ctx)).isEqualTo("en")
  }

  @Test
  fun `detect from header`() {
    every { ctx.header("Accept-Language").valueOrNull() } returns "en-US,en;q=0.9,jp"
    assertThat(Lang.detect(ctx)).isEqualTo("en")
    verify { ctx.setResponseCookie(match { cookie -> cookie.name == Lang.COOKIE && cookie.value == "en" })}
  }

  @Test
  fun `detect from header without country`() {
    every { ctx.header("Accept-Language").valueOrNull() } returns "en;q=0.9,jp"
    assertThat(Lang.detect(ctx)).isEqualTo("en")
  }

  @Test
  fun `fallback to en`() {
    every { ctx.header("Accept-Language").valueOrNull() } returns null
    assertThat(Lang.detect(ctx)).isEqualTo("en")
  }

  @Test @Suppress("UNCHECKED_CAST")
  fun translations() {
    assertThat((Lang.translations["en"]!!["login"] as Map<String, *>)["title"] as String).startsWith("Access")
  }

  @Test
  fun translate() {
    val translate = Lang.translations["en"]!!
    assertThat(translate("login.submit")).isEqualTo("Login")
    assertThat(translate("login")).isEqualTo("login")
    assertThat(translate("login.titleLogin.notExists.yet")).isEqualTo("login.titleLogin.notExists.yet")
  }
}
