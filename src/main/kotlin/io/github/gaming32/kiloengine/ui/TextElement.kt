package io.github.gaming32.kiloengine.ui

import org.joml.Vector2f
import org.joml.Vector2fc

data class TextElement(override var label: String, val font: Font = SCIENTIFICA, override var size: Float = 22f) : SizedUIElement {
    override fun draw(nanovg: Long, location: Vector2fc) {
        val elements = toSubElements()
        val position = Vector2f(location.x(), location.y())

        elements.forEach {
            it.draw(nanovg, position)

            position.x = it.calculateNextCharacterPosition(nanovg, position)
        }
    }

    fun toSubElements(): List<SimpleTextElement> {
        val substrings = mutableListOf<SimpleTextElement>()

        var lastIndex = 0
        var index: Int

        do {
            index = label.indexOf(FORMAT, lastIndex, true)

            if (index == -1) break

            substrings += SimpleTextElement(label.substring(lastIndex + 2, index - 1), when (label[index + 1]) {
                FORMAT_ITALICS -> font.italic
                FORMAT_BOLD -> font.bold
                FORMAT_ITALICS_BOLD -> font.italicBold

                else -> null
            } ?: font.regular, size)

            lastIndex = index
        } while (true)

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