package io.github.gaming32.kiloengine.ui.debug

import io.github.gaming32.kiloengine.ui.FORMAT
import io.github.gaming32.kiloengine.ui.FORMAT_BOLD
import io.github.gaming32.kiloengine.ui.FORMAT_REGULAR
import io.github.gaming32.kiloengine.ui.FORMAT_WHITE
import io.github.gaming32.kiloengine.util.ColoredCoordinates

typealias DebugData = () -> String?

data class SimpleDebugMenuItem(val name: String, val value: DebugData) : DebugMenuItem {
    override fun toString() = "$FORMAT$FORMAT_BOLD$name: $FORMAT$FORMAT_REGULAR${value()}"

    companion object {
        @JvmStatic
        fun ofColoredCoordinates(name: String?, vararg args: Pair<String, DebugData>) =
            SimpleDebugMenuItem(buildString {
                name?.let {
                    append("$FORMAT$FORMAT_WHITE$it ")
                }

                args.forEachIndexed { index, pair ->
                    if (index != 0) append('/')

                    append(ColoredCoordinates.fromIndex(index).createFormattedString(pair.first))
                }

            }) {
                buildString {
                    args.forEachIndexed { index, pair ->
                        if (index != 0) append("/")

                        append(ColoredCoordinates.fromIndex(index).createFormattedString(pair.second()))
                    }
                }
            }
    }
}