package io.github.gaming32.kiloengine.loader

import io.github.gaming32.kiloengine.Level
import io.github.gaming32.kiloengine.model.Model

class ProxyLevelLoader(val loader: LevelLoader, val root: String = "") : LevelLoader {
    override fun loadObj(name: String) = loader.loadObj(root + name)

    override fun loadMaterialLibrary(name: String) = loader.loadMaterialLibrary(root + name)

    override fun loadCollision(model: Model, name: String) = loader.loadCollision(model, root + name)

    override fun loadLevel(name: String, level: Level) = loader.loadLevel(root + name, level)
}
