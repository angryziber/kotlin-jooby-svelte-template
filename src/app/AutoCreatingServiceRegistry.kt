package app

import io.jooby.Jooby
import io.jooby.ServiceKey
import io.jooby.ServiceRegistry
import io.jooby.exception.RegistryException
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException

class AutoCreatingServiceRegistry(private val original: ServiceRegistry): ServiceRegistry by original {
  private val log = LoggerFactory.getLogger(javaClass)

  override fun <T: Any?> getOrNull(key: ServiceKey<T>): T? =
    original.getOrNull(key) ?: autoCreateService(key.type).also { put(key, it) }

  @Suppress("UNCHECKED_CAST")
  private fun <T> autoCreateService(type: Class<T>): T? {
    if (type.packageName == "java.lang") return null
    val constructor = type.constructors.minByOrNull { it.parameterCount } ?: return null
    try {
      val args = constructor.parameters.map { require(it.type) }.toTypedArray()
      return (constructor.newInstance(*args) as T).also {
        log.info("Auto-created ${type.simpleName}${args.map {it.javaClass.simpleName}}")
      }
    }
    catch (e: InvocationTargetException) {
      throw e.targetException
    }
    catch (e: RegistryException) {
      throw RegistryException("Failed to auto-create ${type.simpleName} with dependencies on ${constructor.parameters.map{it.type.simpleName}}: ${e.message}")
    }
  }
}

inline fun <reified T> Jooby.isStub() = environment.isDev || System.getProperty(T::class.simpleName!!) == "stub"
