package io.github.gaming32.fungame.util

import org.jetbrains.annotations.ApiStatus

interface Destroyable : AutoCloseable {
    fun destroy()

    @ApiStatus.NonExtendable
    override fun close() = destroy()
}
