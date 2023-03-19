package io.github.gaming32.kiloengine.loader

import io.github.gaming32.kiloengine.Scene
import io.github.gaming32.kiloengine.model.Material
import io.github.gaming32.kiloengine.model.Model

interface SceneLoader {
    fun loadOBJ(name: String): Model

    fun loadMaterialLibrary(name: String): Map<String, Material>

    fun loadScene(name: String, scene: Scene = Scene()): Scene
}
