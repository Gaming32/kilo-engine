package io.github.gaming32.kiloengine.ui

data class Font @JvmOverloads constructor(
    val regular: String,
    val bold: String? = null,
    val italic: String? = null,
    val italicBold: String? = null
) {
    val isBoldSupported : Boolean
        get() = bold != null

    val isItalicSupported : Boolean
        get() = italic != null

    val isItalicBoldSupported : Boolean
        get() = italicBold != null
}