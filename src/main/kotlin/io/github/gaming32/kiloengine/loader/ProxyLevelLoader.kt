package io.github.gaming32.kiloengine.loader

import io.github.gaming32.kiloengine.Level

class ProxyLevelLoader(val loader: LevelLoader, val root: String = "") : LevelLoader {
    override fun loadObj(name: String) = loader.loadObj(root + name)

    override fun loadMaterialLibrary(name: String) = loader.loadMaterialLibrary(root + name)

    override fun loadLevel(name: String, level: Level) = loader.loadLevel(root + name, level)
}
