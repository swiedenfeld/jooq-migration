plugins {
    java
    id("jooq-conventions")
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.diffplug.spotless") version "6.25.0"
    id("com.avast.gradle.docker-compose") version "0.17.6"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
}

group = "com.opitzconsulting.cattlecrew"
version = "0.0.1-SNAPSHOT"

ext {
    set("jdbcUsername", "jooq_demo_admin") // overwritten per environment
    set("jdbcPassword", "jooq_demo_admin") // overwritten per environment
    set("jdbcUrl", "jdbc:postgresql://localhost:5432/jooq_demo")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jooq-migration"))
    implementation(project(":db"))
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.liquibase:liquibase-core")
    implementation("net.datafaker:datafaker:2.1.0")
    implementation("org.jetbrains:annotations:24.1.0")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

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
