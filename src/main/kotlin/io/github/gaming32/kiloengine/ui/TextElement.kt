package io.github.gaming32.kiloengine.ui

import io.github.gaming32.kiloengine.util.formatCodeToColor
import org.joml.Vector2f
import org.joml.Vector2fc
import org.lwjgl.nanovg.NVGColor

open class TextElement(
    override var label: String,
    val font: Font = DEFAULT_FONT_FAMILY,
    override var size: Float = 22f,
    override var color: NVGColor = WHITE,
    override var backgroundColor: NVGColor? = null,
    override var radius: Float? = null
) : AbstractBoxElement, AbstractTextElement {

    override fun draw(nanovg: Long, location: Vector2fc) {
        val elements = toSubElements()
        val position = Vector2f(location.x(), location.y())

        elements.forEach {
            it.draw(nanovg, position)

            position.x += it.width(nanovg)
        }

        super.draw(nanovg, location)
    }

    fun toSubElements(): List<SimpleTextElement> {
        if (label.isEmpty()) return listOf()

        val substrings = mutableListOf<SimpleTextElement>()

        var index = label.indexOf(FORMAT, ignoreCase = true)
        var nextIndex: Int

        var currentColor: NVGColor = DEFAULT_TEXT_COLOR
        var currentFont: String = font.regular

        while (index != label.length) {
            nextIndex = label.indexOf(FORMAT, index + 1, true)

            if (nextIndex == -1) {
                nextIndex = label.length
            }

            currentColor = formatCodeToColor(label[index + 1]) ?: currentColor
            currentFont = font.formatCodeToFont(label[index + 1]) ?: currentFont

            val substring = label.substring(index + 2, nextIndex)

            if (substring.isNotEmpty()) substrings += SimpleTextElement(substring, currentFont, size, currentColor)


            index = nextIndex
        }

        return substrings.toList()
    }

    override fun width(nanovg: Long): Float {
        var result = 0f

        toSubElements().forEach {
            result += it.width(nanovg)
        }

        return result
    }

    override fun height(nanovg: Long): Float = super.height(nanovg)
}