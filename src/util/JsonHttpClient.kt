package util

import app.httpClient
import app.objectMapper
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers.ofString
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers.ofInputStream
import java.time.Duration
import java.time.Duration.ofSeconds
import kotlin.reflect.KClass

typealias RequestModifier =  HttpRequest.Builder.() -> HttpRequest.Builder

class JsonHttpClient(
  private val urlPrefix: String = "",
  private val reqModifier: RequestModifier = { this },
  private val errorHandler: (HttpResponse<*>, String) -> Nothing = { res, body -> throw IOException("Failed with ${res.statusCode()}: $body") },
  val json: ObjectMapper = objectMapper,
  private val http: HttpClient = httpClient
) {
  private val logger = LoggerFactory.getLogger(javaClass)

  private fun jsonReq(urlSuffix: String) = HttpRequest.newBuilder().uri(URI.create("$urlPrefix$urlSuffix"))
    .setHeader("Content-Type", "application/json; charset=UTF-8").setHeader("Accept", "application/json")
    .timeout(ofSeconds(10)).reqModifier()

  suspend fun <T: Any> request(urlSuffix: String, builder: HttpRequest.Builder.() -> HttpRequest.Builder, type: KClass<T>): T {
    val req = jsonReq(urlSuffix).builder().build()
    val start = System.nanoTime()
    val res = http.sendAsync(req, ofInputStream()).await()
    val ms = (System.nanoTime() - start) / 1000_000
    if (res.statusCode() < 300) {
      logger.info("${req.method()} $urlSuffix in $ms ms")
      return json.parse(res.body(), type)
    }
    else {
      val body = res.body().readBytes().decodeToString()
      logger.error("Failed ${req.method()} $urlSuffix in $ms ms: ${res.statusCode()}: $body")
      errorHandler(res, body)
    }
  }

  suspend inline fun <reified T: Any> request(urlSuffix: String, noinline builder: HttpRequest.Builder.() -> HttpRequest.Builder) =
    request(urlSuffix, builder, T::class)

  suspend inline fun <reified T: Any> get(urlSuffix: String) = request<T>(urlSuffix) { GET() }
  suspend inline fun <reified T: Any> post(urlSuffix: String, o: Any?) = request<T>(urlSuffix) { POST(ofString(json.stringify(o))) }
  suspend inline fun <reified T: Any> put(urlSuffix: String, o: Any?) = request<T>(urlSuffix) { PUT(ofString(json.stringify(o))) }
  suspend inline fun <reified T: Any> delete(urlSuffix: String, o: Any?) = request<T>(urlSuffix) { DELETE() }
}
