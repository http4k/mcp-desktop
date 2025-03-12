import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
    id("org.graalvm.buildtools.native")
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            imageName.set("http4k-mcp-desktop")
            mainClass.set("org.http4k.mcp.Http4kMcpDesktop")
            useFatJar.set(true)
            sharedLibrary.set(false)
        }
    }
}

tasks {
    withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors = false
            jvmTarget.set(JVM_11)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    java {
        sourceCompatibility = VERSION_11
        targetCompatibility = VERSION_11
    }
}

dependencies {
    api(platform(Http4k.bom))

    api("dev.forkhandles:bunting4k:_")
    api(Http4k.securityOauth)
    api(Http4k.client.websocket)
    api(platform("org.http4k:http4k-realtime-core"))

    testImplementation(platform("org.junit:junit-bom:_"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.http4k:http4k-testing-hamkrest")

//    testImplementation(project(":http4k-mcp-sdk"))
    testImplementation("org.http4k:http4k-server-helidon:_")
}

