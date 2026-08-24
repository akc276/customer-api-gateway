import org.gradle.api.tasks.testing.Test // Lets Kotlin DSL configure the Gradle test task by type.

plugins {
	kotlin("jvm") version "2.3.21" // Kotlin JVM compilation support.
	kotlin("plugin.spring") version "2.3.21" // Opens Spring-managed Kotlin classes for proxies.
	id("org.springframework.boot") version "4.1.0" // Spring Boot build and packaging tasks.
	id("io.spring.dependency-management") version "1.1.7" // Uses Spring Boot's compatible dependency versions.
}

group = "com.example.gateway"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21) // Enforces Java 21 for every Gradle build.
	}
}

repositories {
	mavenCentral()
}

dependencies {
	developmentOnly("org.springframework.boot:spring-boot-devtools") // Available locally, excluded from production packaging.

	// Core Web, Actuator, & Kafka Starters
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.kafka:spring-kafka") // Spring Boot starter for Kafka/EventHubs operations
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// Kotlin & Jackson Reflection
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")

	// Distributed Tracing & Spring AOP (@Observed support)
	implementation("org.springframework.boot:spring-boot-micrometer-tracing") // Enables Spring Boot Tracer auto-configuration
	implementation("org.springframework.boot:spring-boot-micrometer-tracing-brave") // Enables Spring Boot Brave auto-configuration
	implementation("io.micrometer:micrometer-tracing-bridge-brave") // Brave bridge for W3C trace context & Kafka headers
	implementation("org.springframework:spring-aop") // Spring AOP support for @Observed aspect
	implementation("org.aspectj:aspectjweaver") // AspectJ weaver for @Observed aspect proxy

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

kotlin {
	compilerOptions {
		// Treat Java nullability annotations strictly and apply annotations to Kotlin constructor properties.
		freeCompilerArgs.addAll(
			"-Xjsr305=strict",
			"-Xannotation-default-target=param-property",
		)
	}
}

tasks.named<Test>("test") {
	useJUnitPlatform() // Runs JUnit 5 tests.
}