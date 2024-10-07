plugins {
    id("org.springframework.boot") version "3.3.3"
    id("java")
}

group = "ru.tbank"
version = "1.0-SNAPSHOT"

val springBootVersion = "3.3.3"
val jacksonVersion = "2.13.0"
val lombokVersion = "1.18.34"
val junitVersion = "5.8.1"
val log4jVersion = "2.20.0"
val apacheCommonsVersion = "3.17.0"

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

    // aop
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation(project(":aop-starter"))

    // tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation(project(mapOf("path" to ":")))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

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
    compileOnly("org.apache.commons:commons-lang3:$apacheCommonsVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType(JavaCompile::class) {
    options.compilerArgs.add("-parameters")
}
