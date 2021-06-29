package app

import auth.Role.USER
import auth.User

/** Reusable entities to use in tests to avoid specifying all params */
object TestData {
  val user = User(login = "login", role = USER, lang = "en")
}
