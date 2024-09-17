// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    kotlin("kapt") version "2.0.10"
    alias(libs.plugins.compose.compiler) apply false
    id ("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
}