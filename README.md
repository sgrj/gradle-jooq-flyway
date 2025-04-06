# Generating JOOQ sources using Flyway and Testcontainers

This is an example project from my blog post on [generating JOOQ sources using Flyway and Testcontainers](http://seb.jambor.dev/posts/generating-jooq-sources-using-flyway-and-testcontainers/). It demonstrates how to set up Gradle tasks for JOOQ so that the JOOQ sources are always in sync with the latest database schema, while ensuring that the Gradle setup is independent of the host system.

The Gradle project itself is very minimal. It only contains the Gradle build file and a dummy migration file. It doesn't even contain any application or test code.

Run `./gradlew generateJooq` to generate the JOOQ sources; they are output to `build/generated-src/jooq`.
