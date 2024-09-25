plugins {
    id("java")
}

group = "ru.tbank"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    // jackson
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.0")

    // lombok
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // log4j2
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}