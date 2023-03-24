package io.github.gaming32.kiloengine.ui

import org.joml.Vector2f
import org.joml.Vector2fc
import org.lwjgl.nanovg.NanoVG.*

data class SimpleTextElement(override var label: String, var font: String, override var size: Float = 22f) : SizedUIElement {

    override fun draw(nanovg: Long, location: Vector2fc) {
        nvgFontFace(nanovg, font)
        nvgFontSize(nanovg, size)

        nvgText(nanovg, location.x(), location.y(), label)
    }

    override fun width(nanovg: Long) = calculateNextCharacterPosition(nanovg, Vector2f(0f, 0f))

    fun calculateNextCharacterPosition(nanovg: Long, location: Vector2fc) : Float {
        nvgFontFace(nanovg, font)
        nvgFontSize(nanovg, size)

        return nvgTextBounds(nanovg, location.x(), location.y(), label, floatArrayOf(0f, 0f, 0f, 0f))
    }
}