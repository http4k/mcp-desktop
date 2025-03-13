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
}

val http4kVersion: String by project

sourceSets {
    test {
//        kotlin.srcDir("src/examples/kotlin")
    }
}

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            imageName.set("http4k-mcp-desktop")
            mainClass.set("org.http4k.mcp.Http4kMcpDesktop")
            useFatJar.set(true)
            sharedLibrary.set(false)

            buildArgs.add("-O1")
            buildArgs.add("--no-fallback")
        }
    }
}

tasks {
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

    implementation("dev.forkhandles:bunting4k:_")
    implementation(Http4k.securityOauth)
    implementation(Http4k.client.websocket)
    implementation(platform("org.http4k:http4k-realtime-core"))

    testImplementation(platform("org.junit:junit-bom:_"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.http4k:http4k-testing-hamkrest")

//    testImplementation(project(":http4k-mcp-sdk"))
    testImplementation("org.http4k:http4k-server-helidon:_")
}

