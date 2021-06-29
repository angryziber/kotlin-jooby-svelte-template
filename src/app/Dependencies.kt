package app

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.config.Config
import io.jooby.Environment
import io.jooby.Kooby
import io.jooby.put

fun Kooby.registerDependencies() {
  services.put(Environment::class.java, environment)
  services.put(Config::class.java, config)
  services.put(ObjectMapper::class, objectMapper)
}
