package io.github.gaming32.kiloengine.ui.debug

import io.github.gaming32.kiloengine.ui.FORMAT_ITALICS

data class ListedDebugMenuItem(val name : String, val value : List<DebugMenuItem>) : DebugMenuItem {
    constructor(name : String, vararg values : DebugMenuItem) : this(name, values.asList())

    override fun toString() : String {
        val returns = StringBuilder("$FORMAT_ITALICS$name:  ")

        value.forEach { returns.append(it).append(' ') }

        return returns.toString()
    }
}