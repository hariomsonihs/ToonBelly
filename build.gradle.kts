// Only plugin management here
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}



