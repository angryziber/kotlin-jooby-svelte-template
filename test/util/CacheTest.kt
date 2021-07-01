package util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CacheTest {
  var count = 0
  val provider = { ++count } // e.g. { http.get("/something") }

  @Test
  fun `cache does not expire`() {
    val cache = Cache()
    assertThat(cache("key", provider)).isEqualTo(1)
    assertThat(cache("key", provider)).isEqualTo(1)
    assertThat(count).isEqualTo(1)

    Cache.dropExpiredEntries()
    assertThat(cache("key", provider)).isEqualTo(1)
    assertThat(count).isEqualTo(1)
  }

  @Test
  fun `cache expires`() {
    val cache = Cache(1)
    assertThat(cache("key", provider)).isEqualTo(1)
    assertThat(count).isEqualTo(1)

    Thread.sleep(1)

    Cache.dropExpiredEntries()
    assertThat(cache("key", provider)).isEqualTo(2)
    assertThat(count).isEqualTo(2)
  }
}