import org.gradle.internal.deprecation.DeprecatableConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.72"
  kotlin("kapt") version "1.3.72"
  application
}

sourceSets {
  getByName("main").apply {
    java.srcDirs("src")
    resources.srcDirs("src", "i18n", "ui/static").exclude("**/*.kt")
  }
  getByName("test").apply {
    java.srcDirs("test")
    resources.srcDirs("test").exclude("**/*.kt")
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "11"
    freeCompilerArgs = listOf("-Xjsr305=strict -progressive")
    javaParameters = true
  }
}

val test by tasks.getting(Test::class) {
  useJUnitPlatform()
  exclude("ui/**")
}

tasks.register<Test>("e2eTest") {
  useJUnitPlatform()
  include("ui/**")
  if (project.hasProperty("headless"))
    systemProperties["chromeoptions.args"] = "--headless,--no-sandbox,--disable-gpu"
  systemProperties["webdriver.chrome.logfile"] = "build/reports/chromedriver.log"
  systemProperties["webdriver.chrome.verboseLogging"] = "true"
}

repositories {
  jcenter()
}

val joobyVersion = "2.8.0"

dependencies {
  kapt("io.jooby:jooby-apt:$joobyVersion")

  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-script-runtime")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
  implementation("io.jooby:jooby:$joobyVersion")
  implementation("io.jooby:jooby-netty:$joobyVersion")
  implementation("io.jooby:jooby-pebble:$joobyVersion")
  implementation("io.jooby:jooby-jackson:$joobyVersion")
  implementation("io.jooby:jooby-hikari:$joobyVersion")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
  implementation("ch.qos.logback:logback-classic:1.2.3")
  implementation("org.liquibase:liquibase-core:3.8.5")
  implementation("org.postgresql:postgresql:42.2.9")
  implementation("javax.mail:mail:1.4.7")

  testImplementation("io.mockk:mockk:1.9.3")
  testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
  testImplementation("org.assertj:assertj-core:3.15.0")
  testImplementation("com.codeborne:selenide:5.10.0")
  runtimeOnly("com.h2database:h2:1.4.200")
}

tasks.register("downloadDeps") {
  doLast {
    fun Configuration.isDeprecated() = if (this is DeprecatableConfiguration) { resolutionAlternatives != null } else false
    configurations.names
      .map { configurations[it] }
      .filter { it.isCanBeResolved && !it.isDeprecated() }
      .forEach { println("Downloaded deps for ${it}:\n   ${it.resolve().joinToString("\n   ")}") }
  }
}

distributions {
  main {
    contents {
      from("public") {
        into("public")
      }
    }
  }
}

application {
  mainClassName = "LauncherKt"
}
