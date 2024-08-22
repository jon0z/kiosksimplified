
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.1" apply false
    kotlin("android") version "1.9.22" apply false
    kotlin("kapt") version "2.0.10"
}