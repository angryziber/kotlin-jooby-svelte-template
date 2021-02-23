import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
  kotlin("jvm") version "1.4.30"
  kotlin("kapt") version "1.4.30"
}

repositories {
  jcenter()
}

val jvm2dts by configurations.creating {
  extendsFrom(configurations.implementation.get())
}

dependencies {
  val joobyVersion = "2.9.5"
  kapt("io.jooby:jooby-apt:$joobyVersion")

  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-script-runtime")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.4.2")
  implementation("io.jooby:jooby:$joobyVersion")
  implementation("io.jooby:jooby-netty:$joobyVersion")
  implementation("io.jooby:jooby-pebble:$joobyVersion")
  implementation("io.jooby:jooby-jackson:$joobyVersion")
  implementation("io.jooby:jooby-hikari:$joobyVersion")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
  implementation("ch.qos.logback:logback-classic:1.2.3")
  implementation("org.slf4j:jul-to-slf4j:1.7.30")
  implementation("org.liquibase:liquibase-core:4.2.2")
  implementation("org.postgresql:postgresql:42.2.18")
  implementation("javax.mail:mail:1.4.7")

  testImplementation("io.mockk:mockk:1.9.3")
  testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
  testImplementation("org.assertj:assertj-core:3.15.0")
  testImplementation("com.codeborne:selenide:5.10.0")
  runtimeOnly("com.h2database:h2:1.4.200")

  jvm2dts("com.codeborne:jvm2dts:1.2.5")
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
  finalizedBy("generateTSTypes")
}

tasks.register("generateTSTypes") {
  dependsOn("classes")
  doLast {
    val excludeClassNamesRegex = ".*(Service|Repository|Controller|Logger|Job|Module)\$"
    val packagesToGenerateTypesFor = "auth"

    File("ui/api/types.ts").writeText(ByteArrayOutputStream().use { out ->
      project.exec {
        standardOutput = out
        commandLine = """java -classpath ${(jvm2dts + sourceSets.main.get().runtimeClasspath).asPath}
          jvm2dts.Main -exclude $excludeClassNamesRegex 
          $packagesToGenerateTypesFor""".split("\\s+".toRegex())
      }
      out.toString()
    })
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

tasks.register<Copy>("deps") {
  into("$buildDir/libs/deps")
  from(configurations.runtimeClasspath)
}

tasks.jar {
  dependsOn("deps")
  archiveBaseName.set("app")
  manifest {
    attributes(
      "Main-Class" to "LauncherKt",
      "Class-Path" to File("$buildDir/libs/deps").listFiles()?.joinToString(" ") { "deps/${it.name}" }
    )
  }
}
