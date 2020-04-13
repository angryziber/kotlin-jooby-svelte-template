package auth

import io.jooby.AssetHandler
import io.jooby.Context
import io.jooby.exception.BadRequestException
import io.jooby.exception.ForbiddenException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import auth.Role.*
import java.util.*

class AuthModuleTest {
  val user = User(login = "login", role = USER, lang = "et")
  val ctx = mockk<Context>(relaxed = true) {
    every { requestPath } returns "/api/anything"
  }
  val auth = AuthModule().apply {
    userRepository = mockk(relaxed = true) {
      every { get(user.id) } returns user
    }
  }

  @Test
  fun `require @Access annotation at routes`() {
    every { ctx.route.attribute<List<Role>>(Access::class.simpleName!!) } returns null
    assertThrows<BadRequestException> { auth.checkUserAndRole(ctx) }
  }

  @Test
  fun `bypass asset routes`() {
    every { ctx.route.handler } returns AssetHandler()
    auth.checkUserAndRole(ctx)
    verify(exactly = 0) { ctx.setUser(any()) }
  }

  @Test
  fun `init user in context even for PUBLIC routes`() {
    every { ctx.route.attribute<List<Role>>(Access::class.simpleName!!) } returns listOf(PUBLIC)
    every { ctx.sessionOrNull()?.get("userId")?.valueOrNull() } returns user.id.toString()
    auth.checkUserAndRole(ctx)
    verify { ctx.setUser(user) }
  }

  @Test
  fun `require userId attribute`() {
    every { ctx.route.attribute<List<Role>>(Access::class.simpleName!!) } returns listOf(ADMIN)
    every { ctx.sessionOrNull()?.get("userId")?.valueOrNull() } returns null
    assertThrows<ForbiddenException> { auth.checkUserAndRole(ctx) }
  }

  @Test
  fun `user not found`() {
    every { ctx.route.attribute<List<Role>>(Access::class.simpleName!!) } returns listOf(ADMIN)
    every { ctx.sessionOrNull()?.get("userId")?.valueOrNull() } returns UUID.randomUUID().toString()
    assertThrows<ForbiddenException> { auth.checkUserAndRole(ctx) }
  }

  @Test
  fun `role does not match`() {
    every { ctx.route.attribute<List<Role>>(Access::class.simpleName!!) } returns listOf(ADMIN)
    every { ctx.sessionOrNull()?.get("userId")?.valueOrNull() } returns user.id.toString()
    assertThrows<ForbiddenException> { auth.checkUserAndRole(ctx) }
  }

  @Test
  fun `role matches`() {
    every { ctx.route.attribute<List<Role>>(Access::class.simpleName!!) } returns listOf(user.role)
    every { ctx.sessionOrNull()?.get("userId")?.valueOrNull() } returns user.id.toString()
    auth.checkUserAndRole(ctx)
    verify { ctx.setUser(user) }
  }

  @Test
  fun `one of roles matches`() {
    every { ctx.route.attribute<List<Role>>(Access::class.simpleName!!) } returns listOf(ADMIN, user.role)
    every { ctx.sessionOrNull()?.get("userId")?.valueOrNull() } returns user.id.toString()
    auth.checkUserAndRole(ctx)
    verify { ctx.setUser(user) }
  }
}
