import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "no.nav.sokos"


repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}


val db2JccVersion = "11.5.8.0"
val hikaricpVersion = "5.0.1"
val natpryceVersion = "1.6.10.0"
val bigQueryVersion = "2.24.5"

val kotestVersion = "5.6.2"
val mockkVersion = "1.13.5"
val db2TestContainerVersion = "1.18.3"

val prometheusVersion = "1.11.1"
val logbackVersion = "1.4.5"
val logstashVersion = "7.3"
val janionVersion = "3.1.9"
val kotlinLoggingVersion = "3.0.5"

dependencies {
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    // Monitorering
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")

    // Config
    implementation("com.natpryce:konfig:$natpryceVersion")

    //Database
    implementation("com.zaxxer:HikariCP:$hikaricpVersion")
    implementation("com.ibm.db2:jcc:$db2JccVersion")

    //BigQuery
    implementation("com.google.cloud:google-cloud-bigquery:$bigQueryVersion")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    runtimeOnly("org.codehaus.janino:janino:$janionVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    // Test
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("org.testcontainers:db2:$db2TestContainerVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("no.nav.sokos.bigquery.tilbakekreving.ApplicationKt")
}

sourceSets {
    main {
        java {
            srcDirs("$buildDir/generated/src/main/kotlin")
        }
    }
}

tasks {

    withType<ShadowJar>().configureEach {
        enabled = true
        archiveFileName.set("app.jar")
        manifest {
            attributes["Main-Class"] = "no.nav.sokos.bigquery.tilbakekreving.ApplicationKt"
            attributes["Class-Path"] = "/var/run/secrets/db2jcc_license_cisuz.jar"
        }
    }

    ("jar") {
        enabled = false
    }


    withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            showExceptions = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }

        maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
        reports.forEach { report -> report.required.value(false) }
    }
}