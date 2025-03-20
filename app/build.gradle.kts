plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
    alias(libs.plugins.google.gms.google.services)
    jacoco
}



android {
    namespace = "com.reaj.emixer"
    compileSdk = 35
    buildFeatures.aidl = true;

    defaultConfig {
        applicationId = "com.reaj.emixer"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiKey: String = project.findProperty("FIREBASE_API_KEY") as? String ?: "API_KEY_PLACEHOLDER"
        buildConfigField("String", "FIREBASE_API_KEY", "\"$apiKey\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.measurement.api)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.android.material:material:<version>") //Check the latest version


    val navVersion = "2.7.3"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    kapt("androidx.room:room-compiler:$room_version")


    // If this project only uses Java source, use the Java annotationProcessor
    // No additional plugins are necessary
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")

    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$room_version")

    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$room_version")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$room_version")

    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$room_version")

    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")

    // MockK for mocking
    testImplementation("io.mockk:mockk:1.13.10")

        // Unit testing framework
        testImplementation("junit:junit:4.13.2")

        // Mockito for mocking
        testImplementation("org.mockito:mockito-core:3.11.2")

        testImplementation ("org.mockito:mockito-inline:3.11.2")

        // Mockito Kotlin extension to make mocking easier in Kotlin
        testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

        testImplementation("org.robolectric:robolectric:4.10.3")

    // Kotlin Coroutines Test library for testing coroutines
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")

        androidTestImplementation ("androidx.test.espresso:espresso-contrib:3.4.0")

}

jacoco {
    toolVersion = "0.8.7" // Certifique-se de usar a versão correta
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*")

    val javaClasses = fileTree("$buildDir/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val kotlinClasses = fileTree("$buildDir/intermediates/classes/debug") {
        exclude(fileFilter)
    }

    classDirectories.setFrom(files(javaClasses, kotlinClasses))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(fileTree("$buildDir") {
        include("jacoco/testDebugUnitTest.exec")
    })
}

// Task to replace the API key placeholder in google-services.json
tasks.register("replaceApiKey") {
    doLast {
        val apiKey = project.findProperty("FIREBASE_API_KEY") as? String ?: "API_KEY_PLACEHOLDER"
        val jsonFile = file("google-services.json")
        val content = jsonFile.readText()
        val newContent = content.replace("API_KEY_PLACEHOLDER", apiKey)
        jsonFile.writeText(newContent)
    }
}

// Make sure the replaceApiKey task runs before the build
tasks.named("preBuild") {
    dependsOn("replaceApiKey")
}