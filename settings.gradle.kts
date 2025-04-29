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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Agregar el repositorio de JitPack para las dependencias de ZEGO
        maven { url = uri("https://jitpack.io") }
        // Agregar el repositorio de ZEGO si es necesario
        maven { url = uri("https://storage.zego.im/maven") }
    }
}

rootProject.name = "xdd"
include(":app")