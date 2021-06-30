package app

import io.jooby.Jooby
import io.jooby.exception.RegistryException
import io.jooby.put
import io.jooby.require
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.security.SecureRandom
import javax.sql.DataSource

class AutoCreatingServiceRegistryTest {
  private val app = Jooby()
  private val registry = AutoCreatingServiceRegistry(app.services)

  @Test
  fun `delegate to original ServiceRegistry`() {
    val db = mockk<DataSource>()
    app.services.put(DataSource::class, db)
    app.registry(registry)
    assertThat(app.require<DataSource>()).isSameAs(db)
  }

  @Test
  fun `auto-creates services with constructor dependencies`() {
    val service = registry.require<DummyDependingService>()
    assertThat(service.dummy1).isInstanceOf(DummyService1::class.java)
    assertThat(service.dummy2).isInstanceOf(DummyService2::class.java)
    assertThat(service.dummy3.random).isSameAs(service.dummy2.random)
    assertThat(service.dummy3.default).isEqualTo("default")
  }

  class DummyService1(val blah: Long = 123)
  class DummyService2(val random: SecureRandom)
  class DummyService3 @JvmOverloads constructor(val random: SecureRandom, val default: String = "default")
  class DummyDependingService(val dummy1: DummyService1, val dummy2: DummyService2, val dummy3: DummyService3)
}
