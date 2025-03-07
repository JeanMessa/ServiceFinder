import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.tcc.tcc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tcc.tcc"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }



    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
        }
    }


}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging-ktx")



    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.firebase:firebase-messaging:24.0.2")
    testImplementation("junit:junit:4.13.2")
    implementation ("androidx.fragment:fragment:1.8.4");
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation ("androidx.work:work-runtime:2.9.1")


    implementation("com.github.santalu:maskara:1.0.0")

    implementation("com.jaredrummler:material-spinner:1.3.1")

    implementation ("com.sun.mail:android-mail:1.6.3")
    implementation ("com.sun.mail:android-activation:1.6.3")

    implementation ("com.github.bumptech.glide:glide:4.16.0")

    implementation ("commons-net:commons-net:3.11.1")

    implementation ("com.android.volley:volley:1.2.1")

    implementation ("com.google.auth:google-auth-library-oauth2-http:1.19.0")

}
