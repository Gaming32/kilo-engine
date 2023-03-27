package io.github.gaming32.kiloengine.ui

import org.joml.Vector2f
import org.joml.Vector2fc

data class TextElement(override var label: String, val font: Font = DEFAULT_FONT_FAMILY, override var size: Float = 22f) : AbstractTextElement {
    override fun draw(nanovg: Long, location: Vector2fc) {
        val elements = toSubElements()
        val position = Vector2f(location.x(), location.y())

        elements.forEach {
            it.draw(nanovg, position)

            position.x += it.width(nanovg)
        }
    }

    fun toSubElements(): List<SimpleTextElement> {
        val substrings = mutableListOf<SimpleTextElement>()

        var index = label.indexOf(FORMAT, ignoreCase = true)
        var nextIndex : Int

        while (index != label.length) {
            nextIndex = label.indexOf(FORMAT, index + 1, true)

            if (nextIndex == -1) {
                nextIndex = label.length
            }

            substrings += SimpleTextElement(label.substring(index + 2, nextIndex), when (label[index + 1]) {
                FORMAT_ITALICS -> font.italic()
                FORMAT_BOLD -> font.bold()
                FORMAT_ITALICS_BOLD -> font.italicBold()

                else -> font.regular
            }, size)

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
}