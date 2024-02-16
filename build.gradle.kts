plugins {
    java
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.diffplug.spotless") version "6.25.0"
    id("com.avast.gradle.docker-compose") version "0.17.6"
    id("org.jooq.jooq-codegen-gradle") version "3.19.3"
}

group = "com.opitzconsulting.cattlecrew"
version = "0.0.1-SNAPSHOT"

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
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.instancio:instancio-junit:4.2.0")
    jooqCodegen("org.postgresql:postgresql:42.5.4")
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
            url = "jdbc:postgresql://localhost:5432/jooq_demo"
            user = "jooq_demo_admin"
            password = "jooq_demo_admin"

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
    }
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
    maxHeapSize = "640m"
}
