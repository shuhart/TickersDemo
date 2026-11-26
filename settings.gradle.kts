pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    // Keep this version aligned with agp in gradle/libs.versions.toml.
    id("com.android.settings") version "9.5.3"
}

android {
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }
    minSdk {
        version = release(24)
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TickersDemo"
include(":app", ":data", ":network", ":ui")
