package app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.mitchellbosecke.pebble.error.LoaderException
import db.BusinessException
import io.jooby.*
import io.jooby.MediaType.html
import io.jooby.MediaType.json
import io.jooby.StatusCode.*
import io.jooby.exception.StatusCodeException
import org.slf4j.LoggerFactory
import util.stringify

class ExceptionHandler: Extension {
  private val errorLogger = LoggerFactory.getLogger("error")

  override fun install(app: Jooby): Unit = (app as Kooby).run {
    errorCode(LoaderException::class.java, NOT_FOUND)
    errorCode(NoSuchElementException::class.java, NOT_FOUND)
    errorCode(MissingKotlinParameterException::class.java, BAD_REQUEST)
    errorCode(IllegalArgumentException::class.java, BAD_REQUEST)
    errorCode(BusinessException::class.java, UNPROCESSABLE_ENTITY)
    error(HtmlStatusCodeException::class.java) { ctx, e, statusCode ->
      ctx.sendJson(mapOf("statusCode" to statusCode.value(), "message" to e.message, "html" to true))
    }
    error(SERVER_ERROR) { ctx, e, statusCode ->
      errorLogger.error("Unhandled error", e)
      if (ctx.accept(listOf(html, json)) == json)
        ctx.sendJson(mapOf("statusCode" to statusCode.value(), "message" to "errors.technical"))
    }
  }

  private fun Context.sendJson(map: Map<String, Any?>) {
    responseType = json
    send(require<ObjectMapper>().stringify(map))
  }
}

class HtmlStatusCodeException(statusCode: StatusCode, message: String): StatusCodeException(statusCode, message)
