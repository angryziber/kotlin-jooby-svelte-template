package auth

import com.fasterxml.jackson.annotation.JsonValue

enum class Role(@JsonValue val page: String) {
  PUBLIC("public"),
  USER("user"),
  ADMIN("admin")
}
