import app.App
import io.jooby.runApp

fun main(args: Array<String>) {
  System.setProperty("application.env", System.getenv("ENV") ?: "dev,test-data")
  runApp(args, App::class)
}
