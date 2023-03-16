package io.github.gaming32.kiloengine.loader

import io.github.gaming32.kiloengine.Scene

class ProxySceneLoader(val loader: SceneLoader, val root: String = "") : SceneLoader {
    override fun loadObj(name: String) = loader.loadObj(root + name)

    override fun loadMaterialLibrary(name: String) = loader.loadMaterialLibrary(root + name)

    override fun loadScene(name: String, scene: Scene) = loader.loadScene(root + name, scene)
}
