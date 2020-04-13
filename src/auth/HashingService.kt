package auth

import java.util.*
import javax.crypto.SecretKeyFactory

import javax.crypto.spec.PBEKeySpec

class HashingService {
  private val iterations = 1000
  private val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
  private val base64 = Base64.getEncoder()

  fun hash(data: String, salt: String): String {
    val chars = data.toCharArray()
    val spec = PBEKeySpec(chars, salt.toByteArray(), iterations, 64 * 8)
    val hash = skf.generateSecret(spec).encoded
    return base64.encodeToString(hash)
  }

  fun hashPassword(password: String, id: UUID) = hash(password, id.toString())
}