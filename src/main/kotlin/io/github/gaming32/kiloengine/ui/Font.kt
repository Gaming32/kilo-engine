package io.github.gaming32.kiloengine.ui

data class Font @JvmOverloads constructor(
    val regular: String,
    val bold: String? = null,
    val italic: String? = null,
    val italicBold: String? = null
) {
    fun bold() = bold ?: regular
    fun italic() = italic ?: regular
    fun italicBold() = italicBold ?: bold ?: italic ?: regular

    fun formatCodeToFont(code: Char) = when (code) {
        FORMAT_ITALICS -> italic()
        FORMAT_BOLD -> bold()
        FORMAT_ITALICS_BOLD -> italicBold()
        FORMAT_REGULAR -> regular

        else -> null
    }
}