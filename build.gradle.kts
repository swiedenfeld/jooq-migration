plugins {
	java
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
	id("com.diffplug.spotless") version "6.25.0"
}

group = "com.opitzconsulting.cattlecrew"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.liquibase:liquibase-core")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
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

tasks.withType<Test> {
	useJUnitPlatform()
}
