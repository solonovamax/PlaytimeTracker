import kotlin.reflect.KProperty

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.quiltmc.org/repository/release") {
            name = "Quilt"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
    }
    
    val loom_version: String by settings
    val kotlin_version: String by settings
    val dokka_version: String by settings
    
    plugins {
        id("org.quiltmc.loom") version loom_version
        kotlin("jvm") version kotlin_version
        id("org.jetbrains.dokka") version dokka_version
    }
    
}

fun <T> Provider<T>.getValue(any: Any?, prop: KProperty<T>): T = get()
