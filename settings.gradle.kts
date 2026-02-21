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
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Local repo with pre-downloaded deps (run scripts/download-offline-deps.ps1 if downloads fail)
        maven {
            url = uri(File(settingsDir, "local-maven-repo").absolutePath)
            content {
                includeGroupByRegex("androidx\\.compose\\.material")
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "AiCalCount"
include(":app")
