package io.github.gaming32.fungame.loader

import io.github.gaming32.fungame.Level
import io.github.gaming32.fungame.model.CollisionModel
import io.github.gaming32.fungame.model.Material
import io.github.gaming32.fungame.model.Model

interface LevelLoader {
    fun loadObj(name: String): Model

    fun loadMaterialLibrary(name: String): Map<String, Material>

    fun loadCollision(model: Model, name: String): CollisionModel

    fun loadLevel(name: String, level: Level = Level()): Level
}
