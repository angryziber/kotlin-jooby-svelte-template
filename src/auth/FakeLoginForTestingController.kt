package auth

import app.Before
import io.jooby.Context
import io.jooby.Environment
import io.jooby.Session
import io.jooby.annotations.*
import io.jooby.exception.NotFoundException

@Path("/fake-login")
class FakeLoginForTestingController(val userRepository: UserRepository, val env: Environment): Before {
  override fun before(ctx: Context) {
    if (!env.isActive("test")) throw IllegalStateException("$javaClass should not be active while not testing")
  }

  @GET("/{login}") @Access(Role.PUBLIC)
  fun fakeLogin(@PathParam login: String, @QueryParam page: String?, session: Session, ctx: Context) {
    session.clear()
    val user = userRepository.byLogin(login) ?: throw NotFoundException("No user: $login")
    session.setUser(user)
    ctx.sendRedirect("/en/app/${page ?: user.role.page}")
  }

  @POST("/session") @Access(Role.PUBLIC)
  fun fakeSessionChange(body: Map<String, String>, session: Session) {
    body.forEach { session.put(it.key, it.value) }
  }
}
