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

    implementation("dev.forkhandles:bunting4k:_")

    runtimeOnly("org.slf4j:slf4j-nop:_")

    implementation("com.jcabi:jcabi-manifests:_")

    implementation(Http4k.securityOauth)
    implementation(Http4k.client.websocket)
    implementation(platform(Http4k.realtimeCore))

    testImplementation(platform(Testing.junit.bom))
    testImplementation(Testing.junit.jupiter.api)
    testImplementation(Testing.junit.jupiter.engine)
    testImplementation(Http4k.testing.hamkrest)

//    testImplementation(project(":http4k-mcp-sdk"))
    testImplementation("org.http4k:http4k-server-helidon:_")
}

