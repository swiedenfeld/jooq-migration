plugins {
    java
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.diffplug.spotless") version "6.25.0"
    id("com.avast.gradle.docker-compose") version "0.17.6"
    id("org.jooq.jooq-codegen-gradle") version "3.19.3"
    id("org.liquibase.gradle") version "2.2.1"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
}

group = "com.opitzconsulting.cattlecrew"
version = "0.0.1-SNAPSHOT"

ext {
    set("jdbcUsername", "jooq_demo_admin") // overwritten per environment
    set("jdbcPassword", "jooq_demo_admin") // overwritten per environment
    set("jdbcUrl", "jdbc:postgresql://localhost:5432/jooq_demo")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    sourceSets.getByName("main").java.srcDir("build/generated-src/jooq/main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.liquibase:liquibase-core")
    implementation("net.datafaker:datafaker:2.1.0")
    implementation("org.jooq:jooq:3.19.3")
    implementation("org.jooq:jooq-meta:3.19.3")
    implementation("org.jooq:jooq-postgres-extensions:3.19.3")
    implementation("org.jooq:jool:0.9.15")
    implementation("org.jetbrains:annotations:24.1.0")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    jooqCodegen("org.postgresql:postgresql:42.5.4")

    liquibaseRuntime("org.liquibase:liquibase-core:4.26.0")
    liquibaseRuntime("org.postgresql:postgresql")
    liquibaseRuntime("info.picocli:picocli:4.7.3")
}

spotless {
    encoding("UTF-8")
    java {
        toggleOffOn()
        targetExclude("build/generated/**")
        palantirJavaFormat()
    }
    kotlin {
        // by default the target is every ".kt" and ".kts` file in the java sourcesets
        ktfmt()
    }
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
            target {
                directory = "build/generated-src/jooq/main"
            }
        }

    }
    executions {
        create("jooq_demo") {
            configuration {
                generator {
                    database {
                        inputSchema = "jooq_demo"
                    }
                    generate {
                        isIndexes = true
                    }
                    target {
                        packageName = "com.opitzconsulting.cattlecrew.jooqmigration.jooq.demo"
                    }
                }
            }
        }
        create("staging") {
            configuration {
                generator {
                    database {
                        inputSchema = "staging"
                    }
                    generate {
                        isIndexes = true
                    }
                    target {
                        packageName = "com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging"
                    }
                }
            }
        }
        create("extensions") {
            configuration {
                generator {
                    database {
                        inputSchema = "extensions"
                    }
                    generate {
                        isIndexes = false
                        isRoutines = true
                        isTables = false

                    }
                    target {
                        packageName = "com.opitzconsulting.cattlecrew.jooqmigration.jooq.extensions"
                    }
                }
            }
        }
    }
}

liquibase {
    activities {
        register("main") {
            this.arguments = mapOf(
                "searchPath" to "${projectDir}/src/main/resources",
                "changelogFile" to "db/changelog/db.changelog.master.yaml",
                "url" to project.ext["jdbcUrl"],
                "username" to project.ext["jdbcUsername"],
                "password" to project.ext["jdbcPassword"]
            )
        }
    }
}

tasks.asciidoctor {
    inputs.dir("${projectDir}/blogs")
    sourceDir {
        "${projectDir}/blogs"
    }
    sources {
        include("*.adoc")
    }

    setOutputDir(file("build/blogs"))


}

tasks.withType<JavaCompile> {
    dependsOn("spotlessApply")
}


dockerCompose {
    waitForTcpPorts = true
    useComposeFiles = listOf("compose.yml")
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "256m"
}
