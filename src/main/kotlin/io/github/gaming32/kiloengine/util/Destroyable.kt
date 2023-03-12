package io.github.gaming32.kiloengine.util

import org.jetbrains.annotations.ApiStatus

interface Destroyable : AutoCloseable {
    fun destroy()

    @ApiStatus.NonExtendable
    override fun close() = destroy()
}
