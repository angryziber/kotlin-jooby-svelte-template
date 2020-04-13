package auth

import io.jooby.Context
import io.jooby.Session
import io.jooby.annotations.GET
import io.jooby.annotations.POST
import io.jooby.exception.UnauthorizedException
import auth.Role.PUBLIC
import java.lang.System.currentTimeMillis

class AuthController(private val userRepository: UserRepository) {
  @POST("/api/auth/login") @Access(PUBLIC)
  fun login(body: AuthRequest, session: Session, ctx: Context): User {
    session.clear()
    return userRepository.byCredentials(body.login, body.password)?.also { user ->
      session.setUser(user)
      ctx.setUser(user)
    } ?: throw UnauthorizedException("login.failed")
  }

  @GET("/logout") @Access(PUBLIC)
  fun logout(session: Session, ctx: Context) {
    session.clear()
    ctx.sendRedirect("/")
  }
}

data class AuthRequest(val login: String, val password: String)

fun Session.setUser(user: User) {
  put("userId", user.id.toString())
  put("loginTime", currentTimeMillis())
}
