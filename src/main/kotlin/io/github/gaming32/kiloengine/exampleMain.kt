package io.github.gaming32.kiloengine

fun main() = object : KiloEngineGame() {
    override val title get() = "Kilo Example Game"

    override fun loadInitScene() {
        sceneLoader.loadScene("/example/example.scene.json5", scene)
    }
}.main()
