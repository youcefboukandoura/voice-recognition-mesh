import org.gradle.kotlin.dsl.publishing

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.youcef.voicereognition"
    compileSdk = 34

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}



dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


publishing {
    publications {
        create<MavenPublication>("release") {
            // Use the release build variant to publish the library
//            from(components["release"])

            // Customize the groupId, artifactId, and version for Maven
            groupId = "com.github.youcefboukandoura"
            artifactId = "voice-recognition-mesh"
            version = "1.0.0"
        }
    }

    repositories {
        // Define where to publish the artifact (e.g., local maven repo or a remote server)
        maven {
            maven { url = uri("https://jitpack.io") }
        }
    }
}