plugins {
    kotlin("jvm") version "2.1.10"
    id("com.gradleup.shadow") version "8.3.6"
}

group = "dev.nikst"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("software.amazon.awssdk:s3:2.25.15")

    testImplementation(kotlin("test"))
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "dev.nikst.MainKt"
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}