package auth

import auth.Role
import db.BaseModel
import java.time.Instant
import java.util.*

data class User(
  override val id: UUID = UUID.randomUUID(),
  val login: String,
  val role: Role,
  val lang: String,
  val name: String? = null,
  val email: String? = null,
  val createdAt: Instant = Instant.now()
): BaseModel
