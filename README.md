# kilo-engine
_A Simple 3D Game Engine_

## Installation
### Gradle Kotlin
Add the following repositories to your `build.gradle.kts`:
```kotlin
repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://maven.jemnetworks.com/releases")
    maven("https://jitpack.io")
    maven("https://maven.jemnetworks.com/snapshots")
}
```
This way, if the version you seek is available as a snapshot, but not as a release, you'll still be able to download it.

Now add the following dependency:
```kotlin
val kiloEngineVersion = "YOUR_VERSION_HERE"

dependencies {
    implementation("io.github.gaming32:kilo-engine:${kiloEngineVersion}")
}
```
This is up-to-date with version `0.1-SNPASHOT`.
### Kotlin
```kotlin
/// game.kts ///

import io.github.gaming32.kiloengine.KiloEngineGame

fun main() = object : KiloEngineGame() {
    override val title get() = "Your Game Name Here"

    override fun loadInitLevel() {
        levelLoader.loadLevel("/example/example.level.json5", level)
        // replace this with your level location
    }
}.main()
```
### Java
```java
/// Game.java ///

import io.github.gaming32.kiloengine.KiloEngineGame;
import org.jetbrains.annotations.NotNull; // <- optional

public final class Game extends KiloEngineGame {
    @NotNull
    @Override
    public String getTitle() {
        return "Your Game Name";
    }

    @Override
    public void loadInitLevel() {
        getLevelLoader().loadLevel("example/example.level.json5", getLevel());
        // replace this with your level name
    }

    public static void main(String[] args) {
        new Game().main();
    }
}
```
