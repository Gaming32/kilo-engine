package io.github.gaming32.fungame.util

interface Drawable {
    fun draw(builder: ModelBuilder)

    fun draw() = buildModel { draw(this) }

    fun toDisplayList() = buildDisplayList { draw(this) }
}
