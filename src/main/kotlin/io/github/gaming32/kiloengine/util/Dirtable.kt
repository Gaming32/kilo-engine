package io.github.gaming32.kiloengine.util

/**
 * Used for classes that can be "marked dirty" - meaning they need a recalculation of some values.
 */
interface Dirtable {
    fun markDirty()

    fun isDirty() : Boolean
}