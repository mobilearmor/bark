plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "com.tree"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    // logging dependencies
    implementation(libs.timber)
    implementation(libs.logback.android)
    implementation(libs.slf4j.api)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

group = "com.github.mobilearmor" // Replace with your GitHub username or organization
version = "0.0.1" // Replace with your desired version

publishing {
    publications {
        create<MavenPublication>("Maven") {

            afterEvaluate {
                from(components["release"])
                groupId = "com.github.mobilearmor"
                artifactId = "bark"
                version = "0.0.1"
            }

        }
    }
}