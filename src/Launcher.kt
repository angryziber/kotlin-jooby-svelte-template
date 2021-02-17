import app.App
import io.jooby.runApp
import org.slf4j.bridge.SLF4JBridgeHandler

fun main(args: Array<String>) {
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()
  System.setProperty("application.env", System.getenv("ENV") ?: "dev,test-data")
  runApp(args, App::class)
}
