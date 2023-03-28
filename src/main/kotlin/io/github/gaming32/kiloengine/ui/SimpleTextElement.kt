package io.github.gaming32.kiloengine.ui

import org.joml.Vector2fc
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG.*

data class SimpleTextElement(
    override var label: String,
    var font: String = DEFAULT_FONT,
    override var size: Float = 22f,
    override var color: NVGColor = WHITE,
) : AbstractTextElement {

    override fun draw(nanovg: Long, location: Vector2fc) {
        nvgFontFace(nanovg, font)
        nvgFontSize(nanovg, size)

        nvgFillColor(nanovg, color)

        nvgText(nanovg, location.x(), location.y(), label)
    }

    override fun width(nanovg: Long): Float {
        nvgFontFace(nanovg, font)
        nvgFontSize(nanovg, size)

        return nvgTextBounds(nanovg, 0f, 0f, label, floatArrayOf(0f, 0f, 0f, 0f))
    }
}