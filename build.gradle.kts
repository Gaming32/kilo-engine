import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.7.21"
    id("io.ktor.plugin") version "2.1.1" // It builds fat JARs
}

group = "io.github.gaming32"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.3.1"
val jomlVersion = "1.10.5"

application {
    mainClass.set("io.github.gaming32.fungame.MainKt")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        attributes["Multi-Release"] = true
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://maven.jemnetworks.com/releases")
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    for (library in listOf("lwjgl", "lwjgl-glfw", "lwjgl-nanovg", "lwjgl-opengl")) {
        implementation("org.lwjgl", library)
        for (natives in listOf(
            "linux", "linux-arm64", "linux-arm32",
            "macos", "macos-arm64",
            "windows", "windows-x86", "windows-arm64"
        )) {
            implementation("org.lwjgl", library, classifier = "natives-$natives")
        }
    }

    implementation("org.joml", "joml", jomlVersion)

    implementation("com.github.rkalla:imgscalr:8ed3644d1e")

    implementation("org.ode4j:core:0.4.0")

    implementation("org.quiltmc:quilt-json5:1.0.2")
    implementation("com.google.code.gson:gson:2.10")
    implementation("io.github.gaming32:gson5-delegate:1.0.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
