pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("de.fayard.refreshVersions").version("0.60.5")
}

refreshVersions {
    enableBuildSrcLibs()

    rejectVersionIf {
        candidate.stabilityLevel.isLessStableThan(current.stabilityLevel)
    }
}

rootProject.name = "http4k-mcp-desktop"
