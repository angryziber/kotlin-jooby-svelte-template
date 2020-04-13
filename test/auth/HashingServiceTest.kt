package auth

import app.toId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HashingServiceTest {
  val hashingService = HashingService()

  @Test
  fun hashPassword() {
    val salt = "ad781678-3cf0-11ea-b77f-2e728ce88125".toId()
    assertThat(hashingService.hashPassword("secret", salt)).isEqualTo("EKquZCQo/g1EzlTVOvb6muWybt1uR9LUEBlSdLmMeHt3hIEO1A6XksokwWMRCfKsm1eNznyxGHsUeNhtzrtlUQ==")
    assertThat(hashingService.hashPassword("Secret", salt)).isNotEqualTo(hashingService.hashPassword("secreT", salt))
  }
}
