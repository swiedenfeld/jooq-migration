
plugins {
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jooq:jooq-codegen-gradle:3.19.3")
    implementation("org.jooq:jooq-meta:3.19.3")
    implementation("org.liquibase.gradle:org.liquibase.gradle.gradle.plugin:2.2.0")
}
