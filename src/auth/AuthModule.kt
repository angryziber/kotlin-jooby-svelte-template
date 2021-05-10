package auth

import app.isHttps
import db.toId
import io.jooby.*
import io.jooby.exception.BadRequestException
import io.jooby.exception.ForbiddenException
import auth.Role.PUBLIC
import java.time.Duration
import java.time.temporal.ChronoUnit.DAYS

class AuthModule: Extension {
  lateinit var userRepository: UserRepository

  override fun install(app: Jooby) {
    val sessionCookie = Cookie("SESSION").setMaxAge(Duration.of(90, DAYS)).setHttpOnly(true)
    if (app.environment.isHttps) sessionCookie.isSecure = true
    app.sessionStore = SessionStore.signed(System.getenv("SESSION_KEY") ?: "Default insecure app session key", sessionCookie)
    userRepository = app.require()
    app.before(::checkUserAndRole)
  }

  fun checkUserAndRole(ctx: Context) {
    if (ctx.route.handler is AssetHandler) return
    val requiredRoles = requiredRoles(ctx)

    val user = findUser(ctx)
    ctx.setUser(user)

    if (!requiredRoles.contains(PUBLIC)) {
      user ?: loginRequired()

      if (!requiredRoles.contains(user.role))
        throw ForbiddenException("Insufficient permissions")
    }
  }

  private fun requiredRoles(ctx: Context): List<Role> {
    var roles = ctx.route.attribute<List<Role>>(Access::class.simpleName!!)
    if (roles == null && !ctx.requestPath.startsWith("/api/"))
      roles = listOf(PUBLIC)
    return roles ?: throw BadRequestException("Role is required for ${ctx.route}")
  }

  private fun findUser(ctx: Context): User? =
    ctx.sessionOrNull()?.get("userId")?.valueOrNull()?.toId()?.let { userId ->
      runCatching { userRepository.get(userId) }.getOrNull()
    }

  private fun loginRequired(): Nothing {
    throw ForbiddenException("Login required")
  }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Access(vararg val value: Role)

val Route.accessPublic get() = attribute(Access::class.simpleName!!, listOf(PUBLIC))
