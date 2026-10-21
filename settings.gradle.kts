pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // GeckoView is hosted on Mozilla's own Maven repository
        maven { url = uri("https://maven.mozilla.org/maven2/") }
    }
}
rootProject.name = "Meghlume"
include(":app")
