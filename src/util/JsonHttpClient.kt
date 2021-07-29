package util

import app.httpClient
import app.objectMapper
import com.fasterxml.jackson.databind.ObjectMapper
import io.jooby.annotations.PATCH
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers.ofString
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.time.Duration
import java.time.Duration.ofSeconds
import kotlin.reflect.KClass

typealias RequestModifier =  HttpRequest.Builder.() -> HttpRequest.Builder

class JsonHttpClient(
  val urlPrefix: String = "",
  val reqModifier: RequestModifier = { this },
  val errorHandler: (HttpResponse<*>, String) -> Nothing = { res, body -> throw IOException("Failed with ${res.statusCode()}: $body") },
  val retryCount: Int = 0,
  val retryAfter: Duration = ofSeconds(1),
  val json: ObjectMapper = objectMapper,
  private val http: HttpClient = httpClient
) {
  val logger = LoggerFactory.getLogger(Exception().stackTrace.first { it.className !== javaClass.name }.className).apply {
    info("Initialized ${JsonHttpClient::class.simpleName} with $urlPrefix")
  }

  private fun jsonReq(urlSuffix: String) = HttpRequest.newBuilder().uri(URI.create("$urlPrefix$urlSuffix"))
    .setHeader("Content-Type", "application/json; charset=UTF-8").setHeader("Accept", "application/json")
    .timeout(ofSeconds(10)).reqModifier()

  private suspend fun <T: Any> request(urlSuffix: String, type: KClass<T>, builder: HttpRequest.Builder.() -> HttpRequest.Builder): T {
    val req = jsonReq(urlSuffix).builder().build()
    val start = System.nanoTime()
    val res = http.sendAsync(req, ofString()).await()
    val ms = (System.nanoTime() - start) / 1000_000
    val body = res.body().trim()
    if (res.statusCode() < 300) {
      logger.info("${req.method()} $urlSuffix in $ms ms: $body")
      return if (type == String::class) body as T else json.parse(body, type)
    }
    else {
      logger.error("Failed ${req.method()} $urlSuffix in $ms ms: ${res.statusCode()}: $body")
      errorHandler(res, body)
    }
  }

  private suspend fun <T: Any> retryRequest(urlSuffix: String, type: KClass<T>, builder: HttpRequest.Builder.() -> HttpRequest.Builder): T {
    for (i in 0..retryCount) {
      try {
        return request(urlSuffix, type, builder)
      } catch (e: IOException) {
        if (i < retryCount) {
          logger.error("$e, retry ${i + 1} after $retryAfter")
          delay(retryAfter.toMillis())
        }
        else {
          logger.error("$urlSuffix: $e")
          throw e
        }
      }
    }
    error("Unreachable")
  }

  suspend fun <T: Any> get(urlSuffix: String, type: KClass<T>) = retryRequest(urlSuffix, type) { GET() }
  suspend inline fun <reified T: Any> get(urlSuffix: String) = get(urlSuffix, T::class)

  suspend fun <T: Any> post(urlSuffix: String, o: Any?, type: KClass<T>) = retryRequest(urlSuffix, type) { POST(ofJson(o)) }
  suspend inline fun <reified T: Any> post(urlSuffix: String, o: Any?) = post(urlSuffix, o, T::class)

  suspend fun <T: Any> put(urlSuffix: String, o: Any?, type: KClass<T>) = retryRequest(urlSuffix, type) { PUT(ofJson(o)) }
  suspend inline fun <reified T: Any> put(urlSuffix: String, o: Any?) = put(urlSuffix, o, T::class)

  suspend fun <T: Any> delete(urlSuffix: String, type: KClass<T>) = retryRequest(urlSuffix, type) { DELETE() }
  suspend inline fun <reified T: Any> delete(urlSuffix: String) = delete(urlSuffix, T::class)

  private fun ofJson(o: Any?): BodyPublisher = ofString(if (o is String) o else json.stringify(o))
}
