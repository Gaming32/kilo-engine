package io.github.gaming32.kiloengine.ui.debug

import io.github.gaming32.kiloengine.KiloEngineGame.Companion.DEBUG_MENU_TEXT_SIZE
import io.github.gaming32.kiloengine.ui.DEFAULT_FONT_FAMILY
import io.github.gaming32.kiloengine.ui.TextElement

interface DebugMenuItem {
    fun toElement() = TextElement(this.toString(), DEFAULT_FONT_FAMILY, DEBUG_MENU_TEXT_SIZE)
}