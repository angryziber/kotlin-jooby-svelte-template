package app

import io.jooby.ServiceKey
import io.jooby.ServiceRegistry
import io.jooby.exception.RegistryException
import io.jooby.require
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class AutoCreatingServiceRegistry(private val original: ServiceRegistry): ServiceRegistry by original {
  private val log = LoggerFactory.getLogger(javaClass)

  override fun <T: Any?> getOrNull(key: ServiceKey<T>): T? =
    original.getOrNull(key) ?: autoCreateService(key.type).also { put(key, it) }

  private fun <T> autoCreateService(type: Class<out T>): T? {
    val constructor = type.kotlin.primaryConstructor ?: type.kotlin.constructors.minByOrNull { it.parameters.size } ?: return null
    try {
      val args = constructor.parameters.filter { !it.isOptional }.associateWith { require(it.type.classifier as KClass<*>) }
      return constructor.callBy(args).also {
        log.info("Auto-created ${type.simpleName}${args.map {it::class.simpleName}}")
      }
    } catch (e: RegistryException) {
      throw RegistryException("Failed to auto-create ${type.simpleName} with dependencies on ${constructor.parameters.map {it.type}}: ${e.message}")
    }
  }
}
