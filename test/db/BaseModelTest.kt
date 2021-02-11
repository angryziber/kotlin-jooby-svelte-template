package db

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BaseModelTest {
  @Test
  fun toValues() {
    val data = TestData("Hello", 123)
    assertThat(data.toValues()).isEqualTo(mapOf("hello" to "Hello", "world" to 123))
  }

  @Test
  fun toValuesSkipping() {
    val data = TestData("Hello", 123)
    assertThat(data.toValuesSkipping(TestData::hello)).isEqualTo(mapOf("world" to 123))
    assertThat(data.toValuesSkipping(TestData::hello, TestData::world)).isEqualTo(emptyMap<String, Any>())
  }

  data class TestData(val hello: String, val world: Int)
}
