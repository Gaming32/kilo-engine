package io.github.gaming32.kiloengine.util

import io.github.gaming32.kiloengine.MatrixStacks
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.lang.ref.Cleaner
import java.nio.FloatBuffer

@PublishedApi
internal const val VERTEX_SIZE = 11

class DisplayList(buffer: FloatBuffer, private val vertexCount: Int) : Destroyable {
    private class DisplayListRef(val buffer: FloatBuffer) : Runnable {
        var open = true

        override fun run() {
            if (open) {
                open = false
                MemoryUtil.memFree(buffer)
            }
        }
    }

    companion object {
        private val CLEANER = Cleaner.create()
    }

    private val list = DisplayListRef(buffer)

    init {
        CLEANER.register(this, list)
    }

    private fun ensureOpen() {
        if (!list.open) {
            throw IllegalStateException("Tried to draw deleted display list $this")
        }
    }

    fun draw(matrices: MatrixStacks? = null) {
        ensureOpen()
        glBufferData(GL_ARRAY_BUFFER, list.buffer, GL_STATIC_DRAW)
        if (matrices != null) {
            MemoryStack.stackPush().use {
                val buffer = it.mallocFloat(16)

                matrices.model.get(buffer)
                glUniformMatrix4fv(matrices.uniModel, false, buffer)

                matrices.projection.get(buffer)
                glUniformMatrix4fv(matrices.uniProjection, false, buffer)
            }
        }
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)
    }

    override fun destroy() = list.run()
}

inline fun buildDisplayList(builder: ModelBuilder.() -> Unit): DisplayList {
    val modelBuilder = ModelBuilder()
    modelBuilder.builder()
    val vertexCount = modelBuilder.elements.size - 1

    val buffer = MemoryUtil.memAllocFloat(vertexCount * VERTEX_SIZE)
    try {
        for (i in 0 until vertexCount) {
            modelBuilder.elements[i].store(buffer)
        }
        buffer.flip()
    } catch (t: Throwable) {
        MemoryUtil.memFree(buffer)
        throw t
    }

    return DisplayList(buffer, vertexCount)
}
