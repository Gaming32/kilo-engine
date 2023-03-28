package io.github.gaming32.kiloengine.ui.debug

import io.github.gaming32.kiloengine.KiloEngineGame.Companion.DEBUG_MENU_FONT_FAMILY
import io.github.gaming32.kiloengine.KiloEngineGame.Companion.DEBUG_MENU_TEXT_COLOR
import io.github.gaming32.kiloengine.KiloEngineGame.Companion.DEBUG_MENU_TEXT_SIZE
import io.github.gaming32.kiloengine.ui.TextElement

interface DebugMenuItem {
    fun toElement() = TextElement(this.toString(), DEBUG_MENU_FONT_FAMILY, DEBUG_MENU_TEXT_SIZE, DEBUG_MENU_TEXT_COLOR)

    @Suppress("unused")
    object EmptyLine : DebugMenuItem {
        override fun toString() = ""
    }
}