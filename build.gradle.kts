import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "no.nav.sokos"


repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

val ktorVersion = "2.2.3"
val db2_jcc_version = "11.5.8.0"
val hikaricp_version = "5.0.1"
val natpryceVersion = "1.6.10.0"
val bigQueryVersion = "2.24.5"

val kotestVersion = "5.5.4"
val mockkVersion = "1.13.4"
val db2TestContainerVersion = "1.17.6"

val prometheusVersion = "1.10.4"
val logbackVersion = "1.4.5"
val logstashVersion = "7.3"
val janionVersion = "3.1.9"
val kotlinLoggingVersion = "3.0.5"

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    // Monitorering
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")

    // Config
    implementation("com.natpryce:konfig:$natpryceVersion")
    testImplementation(kotlin("test"))

    //Database
    implementation("com.zaxxer:HikariCP:$hikaricp_version")
    implementation("com.ibm.db2:jcc:$db2_jcc_version")

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
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.testcontainers:db2:$db2TestContainerVersion")
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("no.nav.sokos.bigquery.tilbakekreving.ApplicationKt")
}

tasks {

    withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(JVM_17)
    }

    withType<ShadowJar>().configureEach {
        enabled = true
        archiveFileName.set("app.jar")
        manifest {
            attributes["Main-Class"] = "no.nav.sokos.bigquery.venteregister.ApplicationKt"
            attributes["Class-Path"] = "/var/run/secrets/db2jcc_license_cisuz.jar"
        }
    }

    ("jar") {
        enabled = false
    }


    withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = FULL
            events("passed", "skipped", "failed")
        }

        maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
        reports.forEach { report -> report.required.value(false) }
    }
}