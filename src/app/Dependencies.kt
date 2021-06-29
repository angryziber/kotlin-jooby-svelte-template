package app

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.typesafe.config.Config
import io.jooby.Environment
import io.jooby.Kooby
import io.jooby.json.JacksonModule
import io.jooby.put
import java.net.http.HttpClient
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS

val objectMapper = JacksonModule.create().disable(WRITE_DATES_AS_TIMESTAMPS).setSerializationInclusion(NON_NULL)
  .configure(FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(KotlinModule())!!

fun Kooby.registerDependencies() {
  services.put(Environment::class.java, environment)
  services.put(Config::class.java, config)
  services.put(HttpClient::class, HttpClient.newBuilder().connectTimeout(Duration.of(5, SECONDS)).build())
  services.put(ObjectMapper::class, objectMapper)
  install(JacksonModule(objectMapper))
}
