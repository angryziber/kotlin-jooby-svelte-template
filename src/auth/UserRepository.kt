package auth

import db.*
import java.security.SecureRandom
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource

class UserRepository(
  db: DataSource,
  private val hashingService: HashingService,
  private val random: SecureRandom
): BaseRepository(db, "users") {
  private val mapper: ResultSet.() -> User = { fromValues() }

  fun get(id: UUID) = db.query(table, id, mapper)

  fun byCredentials(login: String, password: String): User? =
    db.query(table, mapOf("login" to login.lowercase())) {
      mapper().takeIf { hashingService.hashPassword(password, it.id) == getString("passwordHash") }
    }.firstOrNull()

  fun byLogin(login: String): User? =
    db.query(table, mapOf("login" to login.lowercase()), mapper = mapper).firstOrNull()

  fun generatePassword() = (1..16).joinToString("") { (33 + random.nextInt(127 - 33)).toChar().toString() }

  fun create(login: String, role: Role, lang: String, password: String, name: String? = null, email: String? = null): User =
    User(role = role, lang = lang, login = login.lowercase(), name = name, email = email?.lowercase()).also { user ->
      db.insert(table, mapOf(
        "id" to user.id,
        "login" to user.login,
        "role" to user.role,
        "lang" to user.lang,
        "name" to user.name,
        "email" to user.email,
        "passwordHash" to hashingService.hashPassword(password, user.id)
      ))
    }

  fun updatePassword(id: UUID, password: String) =
    db.update(table, mapOf("id" to id), mapOf("passwordHash" to hashingService.hashPassword(password, id)))
}
