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

        // ✅ Add Chaquopy Maven repository
        maven { url = uri("https://chaquo.com/maven") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // ✅ Include Chaquopy repository here too (for safety)
        maven { url = uri("https://chaquo.com/maven") }

        // ✅ Add JitPack repository for MPAndroidChart
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Weather3"
include(":app")