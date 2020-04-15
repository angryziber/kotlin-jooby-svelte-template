package auth

import db.BaseIntegrationTest
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import auth.Role.*
import java.security.SecureRandom

class UserRepositoryTest: BaseIntegrationTest() {
  val random = mockk<SecureRandom>(relaxed = true)
  val repository = UserRepository(db, HashingService(), random)

  @Test
  fun create() {
    val user = repository.create("bob", USER, "en", "pwd")
    assertThat(user.login).isEqualTo("bob")
    assertThat(user.role).isEqualTo(USER)

    val created = repository.get(user.id)
    assertThat(created.copy(createdAt = user.createdAt)).isEqualTo(user)
  }

  @Test
  fun `login is case-insensitive`() {
    val user = repository.create("BoB", ADMIN, "en", "pwd", email = "Blah@Mega.ee")
    assertThat(user.email).isEqualTo("blah@mega.ee")
    val byLogin = repository.byLogin("bOb")
    assertThat(byLogin?.email).isEqualTo(user.email)
    assertThat(byLogin?.id).isEqualTo(user.id)
  }

  @Test
  fun generatePassword() {
    every { random.nextInt(127 - 33) } returnsMany (0..(14 * 5) step 5).toList() + (126 - 33)
    assertThat(repository.generatePassword()).isEqualTo("""!&+05:?DINSX]bg~""")
  }

  @Test
  fun updatePassword() {
    val user = repository.create("bob", USER, "en", "pwd")
    val password = "Bamboozled!"
    repository.updatePassword(user.id, password)
    assertThat(repository.byCredentials("bob", password)?.copy(createdAt = user.createdAt)).isEqualTo(user)
  }
}
