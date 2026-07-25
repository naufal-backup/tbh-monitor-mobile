// Top-level build file. Plugin di-declare di sini (apply false) lalu di-apply per module.
// Catatan: AGP 9.x sudah punya built-in Kotlin support, jadi plugin org.jetbrains.kotlin.android
// tidak perlu di-apply lagi (lihat app/build.gradle.kts).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
