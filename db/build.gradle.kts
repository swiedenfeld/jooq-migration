plugins {
	java
	id("jooq-conventions")
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
