plugins {
    id("jooq-conventions")
}

jooq {
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
                        directory = "build/generated-src/jooq/staging"
                    }
                }
            }
        }
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
                        directory = "build/generated-src/jooq/demo"
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
                        isTables = true
                    }
                    target {
                        withClean(false)
                        packageName = "com.opitzconsulting.cattlecrew.jooqmigration.jooq.extensions"
                        directory = "build/generated-src/jooq/extensions"
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

/**
tasks.named("jooqCodegenJooq_demo") {
    outputs.dir(file("build/generated-src/jooq/demo"))
    inputs.dir(file("src/main/resources/db/changelog")).withPathSensitivity(PathSensitivity.RELATIVE)
}
 */
