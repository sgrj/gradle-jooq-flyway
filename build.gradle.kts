import nu.studer.gradle.jooq.JooqGenerate
import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer

plugins {
  kotlin("jvm") version "1.9.22"
  id("org.flywaydb.flyway") version "11.8.0"
  id("nu.studer.jooq") version "10.1"
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("org.flywaydb:flyway-core:11.8.0")
  implementation("org.flywaydb:flyway-database-postgresql:11.8.0")
  implementation("org.postgresql:postgresql:42.7.5")

  jooqGenerator("org.postgresql:postgresql:42.7.5")
  jooqGenerator("org.testcontainers:postgresql:1.21.0")
  jooqGenerator("org.slf4j:slf4j-simple:2.0.17")
}

buildscript {
  dependencies {
    classpath("org.testcontainers:postgresql:1.21.0")
    classpath("org.postgresql:postgresql:42.7.5")
    classpath("org.flywaydb:flyway-database-postgresql:10.22.0")
  }
}

jooq {
  configurations {
    create("main") {
      jooqConfiguration.apply {
        generator.database.inputSchema = "public"
      }
    }
  }
}

tasks.named<JooqGenerate>("generateJooq") {
  doFirst {
    val dbContainer by project.extra(PostgreSQLContainer("postgres:17").also { it.start() })

    Flyway.configure()
      .locations("filesystem:$projectDir/src/main/resources/db/migration")
      .dataSource(dbContainer.jdbcUrl, dbContainer.username, dbContainer.password)
      .load()
      .migrate()

    jooq {
      configurations {
        getByName("main") {
          jooqConfiguration.jdbc.apply {
            url = dbContainer.jdbcUrl
            username = dbContainer.username
            password = dbContainer.password
          }
        }
      }
    }
  }

  inputs.files(fileTree("src/main/resources/db/migration"))
  allInputsDeclared = true

  finalizedBy("stopDbContainer")
}

tasks.register("stopDbContainer") {
  doLast {
    if (project.extra.has("dbContainer")) {
      val dbContainer: PostgreSQLContainer<*> by project.extra
      dbContainer.stop()
    }
  }
}

sourceSets.main {
  java.srcDir(layout.buildDirectory.dir("generated-src/jooq/main"))
}
