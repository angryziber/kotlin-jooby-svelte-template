package app

import auth.User
import com.fasterxml.jackson.databind.ObjectMapper
import io.jooby.Context
import io.jooby.Environment
import io.jooby.exception.UnauthorizedException
import io.jooby.require

val Environment.isDev get() = isActive("dev")
val Environment.isHttps get() = isActive("https")

val Context.user: User get() = getUser() ?: throw UnauthorizedException()
fun Context.userJson(): String? = require<ObjectMapper>().writeValueAsString(getUser())

val Context.isHttps get() = scheme == "https"
val Context.baseUrl get() = "$requestURL/${Lang.lang(this)}/app"

val Context.requestId get() = header("X-Request-Id").valueOrNull()
