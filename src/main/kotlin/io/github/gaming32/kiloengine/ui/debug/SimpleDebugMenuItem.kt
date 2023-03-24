package io.github.gaming32.kiloengine.ui.debug

import io.github.gaming32.kiloengine.ui.FORMAT_BOLD
import io.github.gaming32.kiloengine.ui.FORMAT_REGULAR

data class SimpleDebugMenuItem(val name : String, val value: () -> String?) : DebugMenuItem {
    override fun toString() = "$FORMAT_BOLD$name: $FORMAT_REGULAR${value()}"
}