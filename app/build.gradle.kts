import com.android.build.api.artifact.SingleArtifact
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Release signing secrets live in the untracked keystore.properties; without it, release builds are unsigned.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) keystorePropertiesFile.inputStream().use { load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.habittracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.marshallhelenm.habitpiggy"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    buildFeatures {
        compose = true
    }
}

// AGP 9 dropped renaming APK outputs in place, so release builds also copy the APK to
// build/outputs/apk/named/HabitPiggy-<versionName>.apk.
androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val copyNamedApk = tasks.register<Copy>("copyNamed${variant.name.replaceFirstChar(Char::uppercase)}Apk") {
            from(variant.artifacts.get(SingleArtifact.APK)) {
                include("*.apk")
                rename { "HabitPiggy-${variant.outputs.single().versionName.get()}.apk" }
            }
            into(layout.buildDirectory.dir("outputs/apk/named"))
        }
        tasks.matching { it.name == "assemble${variant.name.replaceFirstChar(Char::uppercase)}" }
            .configureEach { finalizedBy(copyNamedApk) }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
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
    implementation(libs.google.material)
    // Drag-to-reorder for lazy lists (Goals screen).
    implementation(libs.reorderable)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    // Pinned above lifecycle's transitive 1.7.3: room-testing needs 1.8+, and test APKs must share the app's version.
    implementation(libs.kotlinx.serialization.core)
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    coreLibraryDesugaring(libs.android.desugar.jdk.libs)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.room.testing)
}

// Exported schemas are the history MigrationTestHelper replays; commit them with each version bump.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
