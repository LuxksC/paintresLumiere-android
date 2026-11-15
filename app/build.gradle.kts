import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Google Sign-In needs the OAuth *web* client ID the backend verifies tokens against (Trello PL-77).
// Read it from `google.webClientId` in local.properties or a Gradle property, never from source.
// When it is blank the "Continue with Google" button is simply not shown.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}
/**
 * API base URL per environment (iOS card PL-168, backend card PL-208). Override in
 * local.properties / CI with `api.url.debug`, `api.url.staging`, `api.url.release`; until the
 * backend splits pre-release and production, all three fall back to the current deployment.
 */
val defaultApiUrl = "https://glj3lixjr7.execute-api.sa-east-1.amazonaws.com/"
fun apiUrlFor(buildType: String): String =
    (localProperties.getProperty("api.url.$buildType") ?: providers.gradleProperty("api.url.$buildType").orNull ?: defaultApiUrl)
        .let { if (it.endsWith("/")) it else "$it/" }

val googleWebClientId: String =
    (localProperties.getProperty("google.webClientId") ?: providers.gradleProperty("google.webClientId").orNull).orEmpty()

android {
    namespace = "com.luxkasTech.PaintresLumiere"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.luxkasTech.PaintresLumiere"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
    }

    // Where data comes from. `live` talks to the API; `mock` answers every request from
    // src/main/assets/mocks (see core/network/mock/MockRouter.kt). Pick it in Build Variants.
    flavorDimensions += "backend"
    productFlavors {
        create("live") {
            dimension = "backend"
            isDefault = true
            buildConfigField("boolean", "USE_MOCKS", "false")
        }
        create("mock") {
            dimension = "backend"
            applicationIdSuffix = ".mock"
            versionNameSuffix = "-mock"
            buildConfigField("boolean", "USE_MOCKS", "true")
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"${apiUrlFor("debug")}\"")
            buildConfigField("String", "ENVIRONMENT", "\"dev\"")
        }
        // Release-like build pointed at the pre-release backend, installable next to production.
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            matchingFallbacks += "debug"
            buildConfigField("String", "API_BASE_URL", "\"${apiUrlFor("staging")}\"")
            buildConfigField("String", "ENVIRONMENT", "\"staging\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"${apiUrlFor("release")}\"")
            buildConfigField("String", "ENVIRONMENT", "\"production\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // java.time on minSdk 24 (order dates, timeline).
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    androidResources {
        // Generates the per-app language list (Android 13+ "App languages" setting) from the
        // values-*/ folders, with resources.properties declaring PT-BR as the base locale.
        generateLocaleConfig = true
    }
    lint {
        // A string present in PT-BR but missing in English must fail the build.
        error += "MissingTranslation"
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
