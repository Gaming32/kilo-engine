package io.github.gaming32.kiloengine.ui

import org.joml.Vector2f
import org.lwjgl.nanovg.NVGColor

data class SimpleBoxElement(
    override var label: String,
    val size: Vector2f = Vector2f(),
    override var backgroundColor: NVGColor? = null,
    override var radius: Float? = null
) : AbstractBoxElement {
    override fun width(nanovg: Long): Float = size.x
    override fun height(nanovg: Long): Float = size.y
}