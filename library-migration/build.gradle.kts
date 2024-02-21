import org.jooq.impl.DSL.set
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    id("jooq-conventions")
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.diffplug.spotless") version "6.25.0"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
}

group = "com.opitzconsulting.cattlecrew"
version = "0.0.1-SNAPSHOT"

ext["springShellVersion"] = "3.2.1"

dependencyManagement {
    imports {
        mavenBom("org.springframework.shell:spring-shell-dependencies:${property("springShellVersion")}")
    }
}

dependencies {
    implementation(project(":jooq-migration"))
    implementation(project(":db"))
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.shell:spring-shell-starter")
    implementation("org.liquibase:liquibase-core")
    implementation("net.datafaker:datafaker:2.1.0")
    implementation("org.jetbrains:annotations:24.1.0")

    runtimeOnly("org.postgresql:postgresql:${property("postgresqlVersion")}")
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

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "256m"
}

tasks.create("generateFakeData", BootRun::class) {
    group = "build"
    description = "Generates data for the database"
    mainClass.set("com.opitzconsulting.cattlecrew.jooqmigration.DataGenerator")
}

tasks.create("generateMigrationScripts", BootRun::class) {
    group = "build"
    description = "Generates migration scripts"
    mainClass.set("com.opitzconsulting.cattlecrew.jooqmigration.migration.LibraryMigration")
}
