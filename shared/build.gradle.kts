plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.google.devtools.ksp")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.squareup.sqldelight")
}


sqldelight {
    database("Database") { // This will be the name of the generated database class.
        packageName = "de.danotter.smooothweather.shared.db"
    }
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
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
                implementation(libs.timber)

                implementation(libs.annotation)

                implementation(libs.sqlDelight.coroutines)

                implementation(libs.coroutines.core)

                implementation(libs.kotlinx.datetime)
                implementation(libs.serialization.json)
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.hilt)
                configurations["coreLibraryDesugaring"].dependencies.add(
                    project.dependencies.create(
                        libs.desugaring.get()
                    )
                )

                // https://youtrack.jetbrains.com/issue/KT-58759
                implementation(project.dependencies.platform(libs.compose.bom))
                implementation(libs.coroutines.android)
                implementation(libs.coroutines.play.services)
                implementation(libs.retrofit)
                implementation(libs.okhttp)
                implementation(libs.okhttp.loggingInterceptor)
                implementation(libs.retrofit2.serializationConverter)
                implementation(libs.sqlDelight.androidDriver)
                implementation(libs.playServices.location)
                implementation(libs.compose.googleFonts)
            }
        }
        val iosMain by getting {
            dependencies {
                implementation(libs.sqlDelight.nativeDriver)
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

dependencies {
    add("kspAndroid", project.dependencies.create(libs.hilt.compiler.get()))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}