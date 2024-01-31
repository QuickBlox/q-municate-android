import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.quickblox.qb_qmunicate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.quickblox.qb_qmunicate"
        minSdk = 24
        targetSdk = 33
        versionCode = 300018
        versionName = "3.0.2"

        resourceConfigurations.addAll(listOf("en"))

        testInstrumentationRunner = "com.HiltAndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    fun getLocalProperty(propertyName: String): String {
        return gradleLocalProperties(rootDir).getProperty(propertyName)
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(getLocalProperty("debug_store_file"))
            storePassword = getLocalProperty("debug_store_password")
            keyAlias = getLocalProperty("debug_key_alias")
            keyPassword = getLocalProperty("debug_key_password")
        }

        create("release") {
            storeFile = file(getLocalProperty("release_store_file"))
            storePassword = getLocalProperty("release_store_password")
            keyAlias = getLocalProperty("release_key_alias")
            keyPassword = getLocalProperty("release_key_password")
        }
    }

    buildTypes {
        buildTypes.onEach { type ->
            type.buildConfigField("String", "QB_APPLICATION_ID", getLocalProperty("quickblox_app_id"))
            type.buildConfigField("String", "QB_AUTH_KEY", getLocalProperty("quickblox_auth_key"))
            type.buildConfigField("String", "QB_AUTH_SECRET", getLocalProperty("quickblox_auth_secret"))
            type.buildConfigField("String", "QB_ACCOUNT_KEY", getLocalProperty("quickblox_account_key"))
            type.buildConfigField("String", "QB_API_DOMAIN", getLocalProperty("quickblox_api_endpoint"))
            type.buildConfigField("String", "QB_CHAT_DOMAIN", getLocalProperty("quickblox_chat_endpoint"))
            type.buildConfigField("String", "QB_OPEN_AI_TOKEN", getLocalProperty("quickblox_open_ai_token"))
            type.buildConfigField("String", "QB_AI_PROXY_SERVER_URL", getLocalProperty("quickblox_ai_proxy_server_url"))
            type.buildConfigField("String", "FIREBASE_APP_ID", getLocalProperty("firebase_app_id"))
            type.buildConfigField("String", "TOS_URL", getLocalProperty("tos_url"))
            type.buildConfigField("String", "PRIVACY_POLICY_URL", getLocalProperty("privacy_policy_url"))
        }

        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }

        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // QuickBlox UI-Kit
    implementation("com.quickblox:android-ui-kit:0.9.0")

    // FireBase
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-iid:21.1.0")

    // Coroutines and Kotlin Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-guava:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")

    // DI
    val daggerVersion = "2.49"
    implementation("com.google.dagger:hilt-android:$daggerVersion")
    ksp("com.google.dagger:hilt-android-compiler:$daggerVersion")

    // In-app Update
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Tests
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("com.google.dagger:hilt-android-testing:$daggerVersion")
    kspAndroidTest("com.google.dagger:hilt-compiler:$daggerVersion")
    testImplementation("com.google.dagger:hilt-android-testing:$daggerVersion")
    kspTest("com.google.dagger:hilt-compiler:$daggerVersion")

    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    testImplementation("org.mockito:mockito-core:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
}