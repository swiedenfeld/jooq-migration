plugins {
    id("com.avast.gradle.docker-compose") version "0.17.6"
}
ext {
    set("jdbcUsername", "jooq_demo_admin") // overwritten per environment
    set("jdbcPassword", "jooq_demo_admin") // overwritten per environment
    set("jdbcUrl", "jdbc:postgresql://localhost:5432/jooq_demo")
}
