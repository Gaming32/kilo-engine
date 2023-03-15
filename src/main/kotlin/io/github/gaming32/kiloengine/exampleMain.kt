package io.github.gaming32.kiloengine

fun main() = object : KiloEngineGame() {
    override val title get() = "Kilo Example Game"

    override fun loadInitLevel() {
        levelLoader.loadLevel("/example/example.level.json5", level)
    }
}.main()
