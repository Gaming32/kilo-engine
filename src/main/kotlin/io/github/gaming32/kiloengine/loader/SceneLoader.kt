package io.github.gaming32.kiloengine.loader

import io.github.gaming32.kiloengine.Scene
import io.github.gaming32.kiloengine.mesh.Material
import io.github.gaming32.kiloengine.mesh.Model

interface SceneLoader {
    fun loadOBJ(name: String): Model

    fun loadMaterialLibrary(name: String): Map<String, Material>

    fun loadScene(name: String, scene: Scene = Scene()): Scene
}
