import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.ForcedType

plugins {
  id("org.springframework.boot") version "2.3.3.RELEASE"
  id("io.spring.dependency-management") version "1.0.10.RELEASE"
  id("nu.studer.jooq") version "5.0.1"
  id("org.flywaydb.flyway") version "6.5.5"
  kotlin("jvm") version "1.4.0"
  kotlin("plugin.spring") version "1.4.0"
  kotlin("kapt") version "1.4.0"
}

group = "dev.saljuama.demo"
version = "0.0.1"

flyway {
  url = "jdbc:postgresql://localhost:5432/demo"
  user = "demo"
  password = "demo"
  schemas = arrayOf("public")
}

jooq {
  configurations {
    create("main") {
      jooqConfiguration.apply {
        logging = org.jooq.meta.jaxb.Logging.WARN
        jdbc.apply {
          driver = "org.postgresql.Driver"
          url = "jdbc:postgresql://localhost:5432/demo"
          user = "demo"
          password = "demo"
        }
        generator.apply {
          name = "org.jooq.codegen.DefaultGenerator"
          strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
          database.apply {
            name = "org.jooq.meta.postgres.PostgresDatabase"
            inputSchema = "public"
            excludes = "flyway_schema_history"
            forcedTypes.addAll(arrayOf(
              ForcedType().withName("varchar").withIncludeExpression(".*").withIncludeTypes("JSONB?"),
              ForcedType().withName("varchar").withIncludeExpression(".*").withIncludeTypes("INET")
            ).toList())
          }
          generate.apply {
            isRelations = true
            isDeprecated = false
            isRecords = true
            isImmutablePojos = true
            isFluentSetters = true
          }
          target.apply {
            packageName = "dev.saljuama.demo.nerdalert"
            directory = "src/main/generated"
          }
        }
      }
    }
  }
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
  maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
}

object Versions {
  const val springMockk = "2.0.2"
  const val arrow = "0.10.4"
  const val jjwt = "0.11.2"
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-jooq")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("io.jsonwebtoken:jjwt-api:${Versions.jjwt}")
  implementation("io.jsonwebtoken:jjwt-impl:${Versions.jjwt}")
  implementation("io.jsonwebtoken:jjwt-jackson:${Versions.jjwt}")
  implementation("org.flywaydb:flyway-core")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("io.arrow-kt:arrow-fx:${Versions.arrow}")
  implementation("io.arrow-kt:arrow-syntax:${Versions.arrow}")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  runtimeOnly("org.postgresql:postgresql")
  jooqGenerator("org.postgresql:postgresql")
  kapt("io.arrow-kt:arrow-meta:${Versions.arrow}")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(group = "org.mockito", module = "mockito-core")
    exclude(group = "org.mockito", module = "mockito-junit-jupiter")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.ninja-squad:springmockk:${Versions.springMockk}")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

java.sourceCompatibility = JavaVersion.VERSION_11
tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

tasks.wrapper {
  gradleVersion = "6.6"
  distributionType = Wrapper.DistributionType.ALL
}
