import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "2.3.1.RELEASE"
  id("io.spring.dependency-management") version "1.0.9.RELEASE"
  id("nu.studer.jooq") version "4.2"
  kotlin("jvm") version "1.3.72"
  kotlin("plugin.spring") version "1.3.72"
}

group = "dev.saljuama.demo"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_11

apply(from = "gradle/jooq.gradle")

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-jooq")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.flywaydb:flyway-core")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  runtimeOnly("org.postgresql:postgresql")
  jooqRuntime("org.postgresql:postgresql")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(group = "org.mockito", module = "mockito-core")
    exclude(group = "org.mockito", module = "mockito-junit-jupiter")
  }
  testImplementation("com.ninja-squad:springmockk:2.0.2")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}
