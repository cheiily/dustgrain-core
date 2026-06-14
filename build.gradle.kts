plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "one.cheily"
version = "3.1.2"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.maven.apache.org/maven2") }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:6.1.7")
    testImplementation("io.kotest:kotest-runner-junit5:6.1.7")
    testImplementation("ch.qos.logback:logback-classic:1.5.32")
    testImplementation("org.wiremock:wiremock:3.3.1")
    testImplementation("io.kotest:kotest-extensions-wiremock:6.1.7")
    testImplementation("io.mockk:mockk:1.14.11")

    // core
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.sksamuel.hoplite:hoplite-core:2.9.0")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.9.0")
    implementation("io.ktor:ktor-client-core:3.4.1")
    implementation("io.ktor:ktor-client-cio:3.4.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.1")
    implementation("io.github.oshai:kotlin-logging:8.0.01")
    implementation("net.harawata:appdirs:1.5.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(24)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(
        groupId = group.toString(),
        artifactId = "dustgrain-core",
        version = version.toString()
    )

    pom {
        name = "dustgrain-core"
        description = "A Kotlin library for fetching and formatting data from Dustloop."
        inceptionYear = "2024"
        url = "github.com/cheiily/dustgrain-core"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit/"
            }
        }

        developers {
            developer {
                id = "cheily"
                name = "cheily"
                email = "software@cheily.one"
                organization = "unaffiliated"
                organizationUrl = "https://cheily.one"
            }
        }

        scm {
            url = "https://github.com/cheily/dustgrain-core"
            connection = "scm:git:git://github.com/cheily/dustgrain-core.git"
            developerConnection = "scm:git:ssh://github.com/cheily/dustgrain-core.git"
        }
    }
}
