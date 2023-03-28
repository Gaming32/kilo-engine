@file:Suppress("unused")
@file:JvmName(UTILITIES_CLASS_NAME)
@file:JvmMultifileClass

package io.github.gaming32.kiloengine.util

import io.github.gaming32.kiloengine.ui.*
import org.lwjgl.nanovg.NVGColor
import java.awt.Color

fun Color.toNVGColor() : NVGColor {
    val color = NVGColor.create()
    color.r(red / 255f)
    color.g(green / 255f)
    color.b(blue / 255f)
    color.a(alpha / 255f)
    return color
}

fun toNVG(color: Color) = color.toNVGColor()

fun formatCodeToColor(code: Char) = when(code) {
    FORMAT_BLACK -> BLACK
    FORMAT_DARK_BLUE -> DARK_BLUE
    FORMAT_DARK_GREEN -> DARK_GREEN
    FORMAT_DARK_AQUA -> DARK_AQUA
    FORMAT_DARK_RED -> DARK_RED
    FORMAT_DARK_PURPLE -> DARK_PURPLE
    FORMAT_GOLD -> GOLD
    FORMAT_GRAY -> GRAY
    FORMAT_DARK_GRAY -> GRAY
    FORMAT_BLUE -> BLUE
    FORMAT_GREEN -> GREEN
    FORMAT_AQUA -> AQUA
    FORMAT_RED -> RED
    FORMAT_PURPLE -> PURPLE
    FORMAT_YELLOW -> YELLOW
    FORMAT_WHITE -> WHITE

    else -> null
}

enum class ColoredCoordinates(val formatCode: Char, val color: NVGColor) {
    COORDINATE_RED(FORMAT_RED, RED), COORDINATE_YELLOW(FORMAT_YELLOW, YELLOW), COORDINATE_BLUE(FORMAT_BLUE, BLUE);

    companion object {
        @JvmStatic
        fun fromIndex(i: Int) = ColoredCoordinates.values()[i % 3]
    }

    fun createFormattedString(contents: String?) = "$FORMAT$formatCode$contents$FORMAT$FORMAT_DEFAULT_TEXT_COLOR"
}