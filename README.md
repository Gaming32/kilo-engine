# kilo-engine
Simple 3D game engine

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
val kiloEngineVersion = "0.1-SNAPSHOT"

dependencies {
    implementation("io.github.gaming32:kilo-engine:${kiloEngineVersion}")
}
```

## Basic example

<details>
<summary>Kotlin</summary>

```kotlin
fun main() = object : KiloEngineGame {
    override val title get() = "My Game"
    
    override fun loadInitScene() {
        sceneLoader.loadScene("/my_game/my_scene.scene.json5", scene)
    }
}.main()
```

</details>

<details>
<summary>Java</summary>

```java
public class MyGame extends KiloEngineGame {
    public static void main(String[] args) {
        new MyGame().main();
    }
    
    @Override
    @NotNull
    public String getTitle() {
        return "My Game";
    }
    
    @Override
    public void loadInitScene() {
        getSceneLoader().loadScene("/my_game/my_scene.scene.json5", getScene());
    }
}
```

</details>
