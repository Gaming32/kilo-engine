package io.github.gaming32.kiloengine

fun main() = object : KiloEngineGame() {
    override val skyboxTextures = SkyboxTextures.relative(
        "/example/skybox",
        "down.png",
        "up.png",
        "negativeZ.png",
        "positiveZ.png",
        "negativeX.png",
        "positiveX.png",
    )

    override val title get() = "Kilo Example Game"

    override fun loadInitLevel() {
        levelLoader.loadLevel("/example/example.level.json5", level)
    }
}.main()
