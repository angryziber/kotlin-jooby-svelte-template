package app

import auth.Role
import auth.Role.USER
import auth.User

object TestData {
  val user = User(login = "login", role = USER, lang = "en")
}