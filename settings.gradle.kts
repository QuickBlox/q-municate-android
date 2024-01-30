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
        maven("https://github.com/QuickBlox/quickblox-android-sdk-releases/raw/master/")
        maven("https://github.com/QuickBlox/android-ai-releases/raw/main")
        maven("https://github.com/QuickBlox/android-ui-kit-releases/raw/master/")
    }
}

rootProject.name = "q_municate"
include(":app")
