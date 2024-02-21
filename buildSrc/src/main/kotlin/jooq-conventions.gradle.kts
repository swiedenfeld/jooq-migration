plugins {
    `java-library`
    id("org.jooq.jooq-codegen-gradle")
    id("org.liquibase.gradle")
}

ext {
    set("jdbcUsername", "jooq_demo_admin") // overwritten per environment
    set("jdbcPassword", "jooq_demo_admin") // overwritten per environment
    set("jdbcUrl", "jdbc:postgresql://localhost:5432/jooq_demo")
}

ext["jooqVersion"] = "3.19.4"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    sourceSets.getByName("main").java.srcDir("build/generated-sources/jooq/demo")
    sourceSets.getByName("main").java.srcDir("build/generated-sources/jooq/staging")
    sourceSets.getByName("main").java.srcDir("build/generated-sources/jooq/extensions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jooq:jooq:${property("jooqVersion")}")
    implementation("org.jooq:jooq-meta:${property("jooqVersion")}")
    implementation("org.jooq:jooq-postgres-extensions:${property("jooqVersion")}")
    implementation("org.jooq:jool:0.9.15")

    jooqCodegen("org.postgresql:postgresql:42.5.4")

    liquibaseRuntime("org.liquibase:liquibase-core:4.26.0")
    liquibaseRuntime("org.postgresql:postgresql:42.7.1")
    liquibaseRuntime("info.picocli:picocli:4.7.3")

}
jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = project.ext["jdbcUrl"].toString()
            user = project.ext["jdbcUsername"].toString()
            password = project.ext["jdbcPassword"].toString()
        }
        generator {
            name = "org.jooq.codegen.DefaultGenerator"
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                includes = ".*"
                excludes = "databasechangelog|databasechangeloglock"
            }
        }

    }
}
