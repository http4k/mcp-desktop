import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.graalvm.native)
    alias(libs.plugins.versions)
    alias(libs.plugins.versionCatalogUpdate)
    alias(libs.plugins.typeflows)
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
    implementation(platform(libs.http4k.bom))

    api(libs.bunting4k)

    runtimeOnly(libs.slf4j.nop)

    api(libs.http4k.security.oauth)
    api(libs.http4k.client.websocket)
    api(libs.http4k.realtime.core)
    api(libs.http4k.mcp.sdk)
    api(libs.http4k.mcp.client)

    testApi(platform(libs.junit.bom))
    testApi(libs.junit.jupiter.api)
    testApi(libs.junit.jupiter.engine)
    testApi(libs.http4k.testing.hamkrest)
    testApi(libs.junit.platform.launcher)

    testApi(libs.http4k.server.jetty)
}

sourceSets {
    test {
        kotlin.srcDir("src/examples/kotlin")
    }
}
