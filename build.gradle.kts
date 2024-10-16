plugins {
    id("org.springframework.boot") version "3.3.3"
    id("java")
    jacoco
}

group = "ru.tbank"
version = "1.0-SNAPSHOT"

val springBootVersion = "3.3.3"
val jacksonVersion = "2.13.0"
val lombokVersion = "1.18.34"
val junitVersion = "5.8.1"
val log4jVersion = "2.20.0"
val apacheCommonsVersion = "3.17.0"
val wiremockStandaloneVersion = "3.6.0"
val wiremockTestcontainersVersion = "1.0-alpha-13"
val testContainersVersion = "1.20.2"
val caffeineCacheVersion = "3.1.8"
val resilience4jCircuitBreakerVersion = "2.2.0"
val resilience4jSpringVersion = "2.2.0"
val springDocVersion = "2.6.0"

repositories {
    mavenCentral()
}

dependencies {
    // spring
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(mapOf("group" to "org.junit.vintage", "module" to "junit-vintage-engine"))
    }
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // aop
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation(project(":aop-starter"))

    // tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation(project(mapOf("path" to ":")))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.wiremock:wiremock-standalone:$wiremockStandaloneVersion")
    testImplementation("org.wiremock.integrations.testcontainers:wiremock-testcontainers-module:$wiremockTestcontainersVersion")

    // jackson
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")

    // lombok
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // log4j2
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

    // utils
    implementation("org.apache.commons:commons-lang3:$apacheCommonsVersion")

    // cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeineCacheVersion")

    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // resilience4j
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4jCircuitBreakerVersion")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:$resilience4jSpringVersion")

    // springdoc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")

    // metrics
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // mapstruct
    implementation("org.mapstruct:mapstruct:1.6.2")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType(JavaCompile::class) {
    options.compilerArgs.add("-parameters")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.getByName<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)
    afterEvaluate {
        classDirectories = files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/hw2/**", "**/hw3/**",
                    "**/hw5/dto/**",
                    "**/tbank/datastructure/**"
                )
            }
        })
    }
}
