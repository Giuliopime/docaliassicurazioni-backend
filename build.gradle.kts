import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    id("application")
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.20"

    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "it.docaliassicurazioni"
version = "0.0.1"

application {
    mainClass.set("it.docaliassicurazioni.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("org.litote.kmongo:kmongo:4.2.8")

    implementation("redis.clients:jedis:3.6.3")

    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")

    implementation("io.github.microutils:kotlin-logging:2.0.10")
}

tasks {
    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }
    withType(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    shadowJar {
        archiveFileName.set("docali-backend.jar")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
