plugins {
    kotlin("multiplatform")
    kotlin("kapt")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20"
    id("com.squareup.sqldelight")
}


sqldelight {
    database("Database") { // This will be the name of the generated database class.
        packageName = "de.danotter.smooothweather.shared.db"
    }
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
        extraSpecAttributes["resources"] = "['src/commonMain/resources/**', 'src/iosMain/resources/**']"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation("com.jakewharton.timber:timber:5.0.1")

                implementation("androidx.annotation:annotation:1.7.0-alpha02")

                implementation("com.squareup.sqldelight:coroutines-extensions:1.5.5")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")

                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("com.google.dagger:hilt-android:2.47")
                configurations["kapt"].dependencies.add(project.dependencies.create("com.google.dagger:hilt-compiler:2.47"))
                configurations["coreLibraryDesugaring"].dependencies.add(project.dependencies.create("com.android.tools:desugar_jdk_libs:2.0.3"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.2")
                implementation("com.squareup.retrofit2:retrofit:2.9.0")
                implementation("com.squareup.okhttp3:okhttp:4.11.0")
                implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
                implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
                implementation("com.squareup.sqldelight:android-driver:1.5.5")
                implementation("com.google.android.gms:play-services-location:21.0.1")
                implementation("androidx.compose.ui:ui-text-google-fonts:1.4.3")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("com.squareup.sqldelight:native-driver:1.5.5")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "de.danotter.smooothweather.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}