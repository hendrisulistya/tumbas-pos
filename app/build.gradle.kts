import java.util.Properties
import java.io.FileInputStream
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.tumbaspos.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.tumbaspos.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Exclude unused resources
            excludes += "/META-INF/*.kotlin_module"
            excludes += "DebugProbesKt.bin"
        }
    }
    
    signingConfigs {
        create("release") {
            // Read from environment variables
            val keystoreFile = System.getenv("KEYSTORE_FILE")
            val keystorePass = System.getenv("KEYSTORE_PASSWORD")
            val keyAliasName = System.getenv("KEY_ALIAS")
            val keyPass = System.getenv("KEY_PASSWORD")
            
            // Only set signing config if all environment variables are present
            if (!keystoreFile.isNullOrBlank() && 
                !keystorePass.isNullOrBlank() && 
                !keyAliasName.isNullOrBlank() && 
                !keyPass.isNullOrBlank()) {
                storeFile = file(keystoreFile)
                storePassword = keystorePass
                keyAlias = keyAliasName
                keyPassword = keyPass
            }
        }
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Only apply signing if keystore is configured
            val releaseSigningConfig = signingConfigs.getByName("release")
            if (releaseSigningConfig.storeFile != null) {
                signingConfig = releaseSigningConfig
            }
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    

    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    bundle {
        abi {
            enableSplit = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android.defaultConfig {
    val r2AccountId = localProperties.getProperty("R2_ACCOUNT_ID", "").trim().trim('"')
    val r2AccessKeyId = localProperties.getProperty("R2_ACCESS_KEY_ID", "").trim().trim('"')
    val r2SecretAccessKey = localProperties.getProperty("R2_SECRET_ACCESS_KEY", "").trim().trim('"')
    val r2BucketName = localProperties.getProperty("R2_BUCKET_NAME", "").trim().trim('"')
    val activationSecret = localProperties.getProperty("APP_SECRET", "").trim().trim('"')

    buildConfigField("String", "R2_ACCOUNT_ID", "\"$r2AccountId\"")
    buildConfigField("String", "R2_ACCESS_KEY_ID", "\"$r2AccessKeyId\"")
    buildConfigField("String", "R2_SECRET_ACCESS_KEY", "\"$r2SecretAccessKey\"")
    buildConfigField("String", "R2_BUCKET_NAME", "\"$r2BucketName\"")
    buildConfigField("String", "ACTIVATION_SECRET", "\"$activationSecret\"")
}

dependencies {
    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Activity Compose
    implementation(libs.androidx.activity.compose)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // Core
    implementation(libs.androidx.core.ktx)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    
    // Koin for DI
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.core)
    
    // OkHttp for S3 client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // ML Kit Barcode Scanning (Unbundled)
    implementation(libs.mlkit.barcode.scanning)
    
    // CameraX
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    
    // Printer
    implementation(libs.escpos.printer)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    
    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // Accompanist Permissions
    implementation(libs.accompanist.permissions)
    
    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
