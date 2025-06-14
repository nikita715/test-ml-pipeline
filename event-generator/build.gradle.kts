plugins {
    kotlin("jvm") version "2.1.10"
}

group = "dev.nikst"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.9")
    implementation("io.ktor:ktor-server-netty:2.3.9")

    implementation("org.apache.kafka:kafka-clients:3.6.1")
    implementation("io.confluent:kafka-avro-serializer:7.5.0")
    implementation("org.apache.avro:avro:1.11.1")
    implementation("com.google.guava:guava:32.1.3-jre")

    implementation("org.slf4j:slf4j-simple:2.0.9")

    implementation("com.opencsv:opencsv:5.9")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}