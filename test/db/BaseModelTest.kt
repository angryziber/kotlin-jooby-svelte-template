package db

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BaseModelTest {
  @Test
  fun toValues() {
    val data = SomeData("Hello", 123)
    assertThat(data.toValues()).isEqualTo(mapOf("hello" to "Hello", "world" to 123))
  }

  @Test
  fun toValuesSkipping() {
    val data = SomeData("Hello", 123)
    assertThat(data.toValuesSkipping(SomeData::hello)).isEqualTo(mapOf("world" to 123))
    assertThat(data.toValuesSkipping(SomeData::hello, SomeData::world)).isEqualTo(emptyMap<String, Any>())
  }

  data class SomeData(val hello: String, val world: Int)
}
