val springBootVersion = "3.3.3"
val jacksonVersion = "2.13.0"
val lombokVersion = "1.18.34"
val junitVersion = "5.8.1"
val log4jVersion = "2.20.0"
val apacheCommonsVersion = "3.17.0"

plugins {
    id("java")
}

group = "ru.tbank"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // spring
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation("org.springframework.boot:spring-boot-starter")

    // lombok
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // aop
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // log4j
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

    // tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}