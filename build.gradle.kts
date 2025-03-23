import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
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
    mavenLocal()
}

val http4kVersion: String by project

sourceSets {
    test {
        kotlin.srcDir("src/examples/kotlin")
    }
}

val VERSION: String? by project

version = (VERSION ?: "LOCAL")


val mainMcpDesktopClass = "org.http4k.mcp.Http4kMcpDesktop"

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            imageName.set("http4k-mcp-desktop")
            mainClass.set(mainMcpDesktopClass)
            useFatJar.set(true)
            sharedLibrary.set(false)

            buildArgs.add("-O1")
            buildArgs.add("--no-fallback")
            buildArgs.add("-march=compatibility")
        }
    }
}

tasks {

    register("generateVersionProperties" ) {
        doLast {
            file("src/main/resources/version.properties").apply {
                parentFile.mkdirs()
                writeText("version=${project.version}")
            }
        }
    }

    named("processResources") {
        dependsOn("generateVersionProperties")
    }

    withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors = false
            jvmTarget.set(JVM_21)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    java {
        sourceCompatibility = VERSION_21
        targetCompatibility = VERSION_21
    }
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:$http4kVersion"))

    api("dev.forkhandles:bunting4k:_")

    runtimeOnly("org.slf4j:slf4j-nop:_")

    api("org.http4k:http4k-security-oauth")
    api("org.http4k:http4k-api-jsonrpc")
    api("org.http4k:http4k-client-websocket")
    api("org.http4k:http4k-realtime-core")

    testApi(platform(Testing.junit.bom))
    testApi(Testing.junit.jupiter.api)
    testApi(Testing.junit.jupiter.engine)
    testApi("org.http4k:http4k-testing-hamkrest")

    testApi("org.http4k.pro:http4k-mcp-sdk:LOCAL")
    testApi("org.http4k:http4k-server-helidon")
}

