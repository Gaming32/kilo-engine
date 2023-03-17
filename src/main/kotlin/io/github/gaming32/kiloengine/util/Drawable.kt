package io.github.gaming32.kiloengine.util

interface Drawable {
    fun draw(builder: ModelBuilder)

    fun toDisplayList() = buildDisplayList { draw(this) }
}
