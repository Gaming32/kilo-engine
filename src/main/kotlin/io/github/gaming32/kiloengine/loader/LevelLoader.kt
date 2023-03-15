package io.github.gaming32.kiloengine.loader

import io.github.gaming32.kiloengine.Level
import io.github.gaming32.kiloengine.model.Material
import io.github.gaming32.kiloengine.model.Model

interface LevelLoader {
    fun loadObj(name: String): Model

    fun loadMaterialLibrary(name: String): Map<String, Material>

    fun loadLevel(name: String, level: Level = Level()): Level
}
