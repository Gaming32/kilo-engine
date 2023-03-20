# kilo-engine
_A Simple 3D Game Engine_

## Installation
***Warning:** snpashot versions are a subject to constant, breaking changes, without a version number change. For that reason, a dependency refresh might break your code if you use a snapshot version of the engine. It is recommended to users of snapshot versions (currently there are no public release versions) to keep track of this `README` page and this respository's commit history.*


<details open>
<summary>Gradle Kotlin</summary>

```kotlin
// build.gradle.kts

repositories {
    mavenCentral()
    maven("https://maven.jemnetworks.com/releases")
    maven("https://maven.jemnetworks.com/snapshots")
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://jitpack.io")
}

dependencies {
    implementation("io.github.gaming32:kilo-engine:0.1-SNAPSHOT")
}
```

</details>

<details>
<summary>Gradle Groovy</summary>

```groovy
// build.gradle

repositories {
    mavenCentral()
    maven { url = "https://maven.jemnetworks.com/releases" }
    maven { url = "https://maven.jemnetworks.com/snapshots" }
    maven { url = "https://maven.quiltmc.org/repository/release" }
    maven { url = "https://jitpack.io" }
}

dependencies {
    implementation "io.github.gaming32:kilo-engine:0.1-SNAPSHOT"
}
```

</details>

<details>
<summary>Maven</summary>

```xml
<!-- pom.xml -->

<repositories>
  <repository>
    <id>gaming32</id>
    <name>Gaming32</name>
    <url>https://maven.jemnetworks.com/releases</url>
  </repository>
  <repository>
    <id>gaming32-snapshots</id>
    <name>Gaming32 Snapshots</name>
    <url>https://maven.jemnetworks.com/snapshots</url>
  </repository>
  <repository>
    <id>quiltmc</id>
    <name>QuiltMC</name>
    <url>https://maven.quiltmc.org/repository/release</url>
  </repository>
  <repository>
    <id>jitpack.io</id>
    <name>Jitpack</name>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>io.github.gaming32</groupId>
    <artifactId>kilo-engine</artifactId>
    <version>0.1-SNAPSHOT</version>
  </dependency>
</dependencies>
```

</details>

## Basic example

<details open>
<summary>Kotlin</summary>

```kotlin
fun main() = object : KiloEngineGame {
    init {
        Resources.resourceGetter += javaClass::getResourceAsStream
    }

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
        Resources.addResourceGetter(MyGame.class::getResourceAsStream);
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

## Scenes

A game in Kilo Engine is made up of "scenes" (well actually just one at this point). A scene is a collection of [entities](#entities) that make up the game, as well as various other minor things.

Scenes can be constructed purely through using code, but it is preferred to specify the initial data of scenes through `.scene.json5` files. You can load scenes from Json5 files using `sceneLoader#loadScene`.

<!-- Scenes, however, have a bit more to them than just entities. Specifically, they can also have a skybox and a sun. The skybox is a cubemap defining the sides, a solid color, or nothing. If no skybox is specified, it will default to no skybox. Please do note, however, that a skybox should almost *always* be specified for outdoor scenes, as the color buffer is never cleared, so the sky without a skybox will just be the previous frame. The sun position indicates a position for the sun. The sun will be a directional light from its position to `(0, 0, 0)`. If the sun's position is not specified, no lighting will be automatically applied. -->

Scene Json5 syntax is simple. Here's a brief example:

<details open>
<summary>Sample Json5 scene</summary>

```json5
{
  entities: [
    // What's a level without some entities, eh?
    // More details on entity Json5 syntax below in the "Entities" section.
    {
      kinematic: true,
      components: [
        {
          type: "mesh",
          mesh: "/example/example.obj"
        },
        "meshRenderer",
        {
          type: "meshCollider",
          collision: {
            Brick_Antique_01: "wall",
            Brick_Basket: "floor",
            Death_Plane: "death"
          }
        }
      ]
    },
    {
      position: [0, 1.4, -5],
      components: [
        {
          type: "capsuleCollider",
          radius: 0.4,
          length: 1
        },
        "player",
        {
          type: "camera",
          offset: [0, 0.7, 0],
          skybox: { // A cubemap skybox
            base: "/example/skybox", // *Optional* base path for the paths in this object
            down: "down.png", // Because `base` is specified, this will load from `/example/skybox/down.png`.
            up: "up.png",
            negativeZ: "negativeZ.png",
            positiveZ: "positiveZ.png",
            negativeX: "negativeX.png",
            positiveX: "positiveX.png"
          },
          skybox: [0.75, 0.25, 0.25], // A light red solid color skybox
        }
      ]
    }
  ]
}
```

</details>

So you want to see how this is done in code? Well here you go:

<details>
<summary>The above example, but in Kotlin</summary>

```kotlin
Entity(scene, DVector3()).apply {
    body.setKinematic()
    val model = MeshComponent(this, sceneLoader.loadObj("/example/example.obj")).model
    MeshRendererComponent(this)
    MeshColliderComponent(this, CollisionModel(model, mapOf(
        model.materials["Brick_Antique_01"]!! to CollisionTypes.WALL,
        model.materials["Brick_Basket"]!! to CollisionTypes.FLOOR,
        model.materials["Death_Plane"]!! to CollisionTypes.DEATH
    )))
}
Entity(scene, DVector3(0.0, 1.4, -5.0)).apply {
    CapsuleColliderComponent(this, 0.4, 1.0)
    PlayerComponent(this, Vector2f())
    CameraComponent(
        this, 
        offset = DVector3(0.0, 0.7, 0.0),
        skybox = Skybox.Cubemap.relative(
            "/example/skybox",
            "down.png",
            "up.png",
            "negativeZ.png",
            "positiveZ.png",
            "negativeX.png",
            "positiveX.png"
        )
        // skybox = Skybox.SolidColor(0.75f, 0.25f, 0.25f)
    )
}
```

</details>

<details>
<summary>The above example, but in Java</summary>

```java
final Scene scene = getScene();

final Entity levelMesh = new Entity(scene, new DVector3());
levelMesh.getBody().setKinematic();
final Model model = new MeshComponent(
    levelMesh, getSceneLoader().loadObj("/example/example.obj")
).getModel();
new MeshRendererComponent(levelMesh);
new MeshColliderComponent(levelMesh, new CollisionModel(model, Map.of(
    model.getMaterials().get("Brick_Antique_01"), CollisionTypes.WALL,
    model.getMaterials().get("Brick_Basket"), CollisionTypes.FLOOR,
    model.getMaterials().get("Death_Plane"), CollisionTypes.DEATH
)));

final Entity player = new Entity(scene, new DVector3(0, 1.4, -5));
new CapsuleColliderComponent(player, 0.4, 1);
new PlayerComponent(player, new Vector2f());
new CameraComponent(
    player, 
    new DVector3(0.0, 0.7, 0.0),
    Skybox.Cubemap.relative(
        "/example/skybox",
        "down.png",
        "up.png",
        "negativeZ.png",
        "positiveZ.png",
        "negativeX.png",
        "positiveX.png"
    )
    // new Skybox.SolidColor(0.75f, 0.25f, 0.25f)
);
```

</details>

## Entities
***Warning:** you must add a camera to the scene, otherwise it just won't be rendered.*

Entities are _everything_ in your game. They can be the camera, a light source, or that shiny house model you made.
Everything is an entity, and custom behaviours for those entities - `Components` - can be created and assigned.
```json5
// scene.json5
{
  entities: [
    // this is where the following codeblocks will be in.
  ]
}
```

Each entity is composed of three elements:
1. [Position](#position)
2. [Physics](#physics)
3. [Components](#components)

### Position
The position of the object in the world, in a 3-element array representing a `Vector3f`.
```json5
{
  //           x    y    z
  position: [ 0.0, 0.0, 0.0 ]
}
```

### Physics
***Did you know?** Physics are on by default because otherwise god would've created this universe without them, because he/she/they (or even it) doesn't read READMEs.*

For now, you can only do one thing: enable and disable physics. Play around with the example to experience the physics the engine has to offer.
Remember colliders aren't automatic, but a component is needed for them.
```json5
{
  kinematic: true // disables physics, 
}
```

## Components
These are the *good stuff*. Components are where you code your game!

All components require a `type` - a string that is used to identify the component, and is unique to it.
If a component doesn't have parameters, it can be shortened into a string.
```json5
{
  components: [
    {
      type: "camelCase",
      
      // parameters go here
    },
    
    // can also be wrote as:
    "camelCase"
  ]
}
```

See [`ComponentRegistry.kt`](/src/main/kotlin/io/github/gaming32/kiloengine/entity/ComponentRegistry.kt) for a list of built-in components.

### Mesh
This component is used to add mesh to your object, whether it be a pre-made `.obj` file, or procedurally generated terrain.
```json5
{
  type: "mesh",
  
  fromRegistry: false, // false by default
  mesh: "path or identifier" // required
}
```
#### Rendering
A mesh won't render, unless its entity also has the component `meshRenderer`.
```json5
{
  components: [
    {
      type: "mesh",
      
      mesh: "/my/mesh"
    },
    "meshRenderer"
  ]
}
```

#### Registry
The mesh registry saves mesh _types_ to a unique `identifier` - a string, similarly to component types.

If you set `fromRegistry` to `true`, the value of `mesh` will be used to pull the mesh type from the registry instead of being treated as a file path.

An example of where and how you should register your mesh type:
```kotlin
fun main() = object : KiloEngineGame() {
    /* ... */
    
    override fun loadInitScene() {
        MeshRegistry.register("identifier", MyMeshType())
        
        sceneLoader.loadScene("/example/example.scene.json5", scene)
    }
}.main()
```
An example of a mesh type in a `scene.json5`:
```json5
{
  type: "mesh",
  
  fromRegistry: true,
  mesh: "myIdentifier", // turns into an identifier
  
  myMeshData: 0.4 // only supported in registry meshes
}
```

#### Mesh Types
a `MeshType` is an object that gets the component's json object as input, and returns mesh as output.
It has two uses:
1. **_Creating models on the fly_ -** for example, creating a cube model with a set size using code. This isn't a procedural mesh, as it doesn't change its size (although a cube that changes sizes is an excellent example of procedural mesh uses)
2. **_Creating procedural meshes_ -** see below.

#### Procedural Meshes
Procedural meshes can *only* be called from the registry, as they're not contained in static code, but change
once in a while based on their data, and data they received during initialization (as part of the component).

Procedural meshes can be "marked dirty" using `.markDirty()`. Every tick, the engine checks if the mesh is "dirty".
If it is, it is recalculated. Try to make this method as efficient as you can, and to use separate meshes if possible,
as it results in less things to calculate at once, making your game more efficient.
```kotlin
class MyProceduralMesh : ProceduralMesh() {
    override fun calculateMesh() {
        val triangles = mutableListOf<Mesh.Triangle>()
        
        // do your stuff
        
        return triangles.toList()
    }
    
    override fun getMaterial(identifier: String) : Material? // TODO: document this
}
```

***TODO:** document [physics](#physics) and colliders.*