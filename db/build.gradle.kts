plugins {
    id("jooq-conventions")
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")
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
    executions {
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
                        directory = "build/generated-sources/jooq/staging"
                    }
                }
            }
        }
        create("demo") {
            configuration {
                generator {
                    database {
                        inputSchema = "jooq_demo"
                    }
                    generate {
                        isIndexes = true
                        isNonnullAnnotation = true
                        nonnullAnnotationType = "org.jetbrains.annotations.NotNull"
                        isNullableAnnotation = true
                        nullableAnnotationType = "org.jetbrains.annotations.Nullable"
                    }
                    target {
                        packageName = "com.opitzconsulting.cattlecrew.jooqmigration.jooq.demo"
                        directory = "build/generated-sources/jooq/demo"
                    }
                }
            }
        }
        create("extensions") {
            configuration {
                generator {
                    database {
                        inputSchema = "extensions"
                        excludes= "pg_stat_statements"
                    }
                    generate {
                        isIndexes = true
                        isRoutines = true
                        isTables = false

                    }
                    target {
                        withClean(false)
                        packageName = "com.opitzconsulting.cattlecrew.jooqmigration.jooq.extensions"
                        directory = "build/generated-sources/jooq/extensions"
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


tasks.named("build") {
    dependsOn("jooqCodegen")
}
tasks.named("update") {
    finalizedBy("jooqCodegen")
}

tasks.named("compileJava") {
    dependsOn("jooqCodegen")
}

tasks.named("spotlessJava") {
    mustRunAfter("jooqCodegenDemo", "jooqCodegenStaging", "jooqCodegenExtensions")
}
