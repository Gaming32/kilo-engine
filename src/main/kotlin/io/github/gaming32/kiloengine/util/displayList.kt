package io.github.gaming32.kiloengine.util

import io.github.gaming32.kiloengine.MatrixStacks
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.lang.ref.Cleaner
import java.nio.FloatBuffer

@PublishedApi
internal const val VERTEX_SIZE = 11

class DisplayList(buffer: FloatBuffer, private val textures: IntArray, private val vertexCounts: IntArray) : Destroyable {
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
        require(textures.size == vertexCounts.size) { "textures and vertexCounts must be the same size" }
    }

    private fun ensureOpen() {
        if (!list.open) {
            throw IllegalStateException("Tried to draw deleted display list $this")
        }
    }

    fun draw(matrices: MatrixStacks?) {
        ensureOpen()
        if (matrices != null) {
            MemoryStack.stackPush().use {
                val buffer = it.mallocFloat(16)

                matrices.model.get(buffer)
                glUniformMatrix4fv(matrices.uniModel, false, buffer)

                matrices.projection.get(buffer)
                glUniformMatrix4fv(matrices.uniProjection, false, buffer)
            }
        }
        glBufferData(GL_ARRAY_BUFFER, list.buffer, GL_STREAM_DRAW)
        var vertex = 0
        for (i in textures.indices) {
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, textures[i])
            glDrawArrays(GL_TRIANGLES, vertex, vertexCounts[i])
            vertex += vertexCounts[i]
        }
    }

    override fun destroy() = list.run()
}

inline fun buildDisplayList(builder: ModelBuilder.() -> Unit): DisplayList {
    val modelBuilder = ModelBuilder()
    modelBuilder.builder()
    return finishDisplayList(modelBuilder)
}

@PublishedApi
internal fun finishDisplayList(modelBuilder: ModelBuilder): DisplayList {
    val vertexCount = modelBuilder.elements.size - 1
    val textures = mutableListOf<Int>()
    val vertexCounts = mutableListOf<Int>()
    val buffer = MemoryUtil.memAllocFloat(vertexCount * VERTEX_SIZE)
    try {
        var currentTexture = 0
        var currentVertexCount = 0
        for (i in 0 until vertexCount) {
            val element = modelBuilder.elements[i]
            val newTexture = element.texture
            if (newTexture != currentTexture) {
                if (currentVertexCount > 0) {
                    textures.add(currentTexture)
                    vertexCounts.add(currentVertexCount)
                }
                currentTexture = newTexture
                currentVertexCount = 0
            }
            element.store(buffer)
            currentVertexCount++
        }
        textures.add(currentTexture)
        vertexCounts.add(currentVertexCount)
        buffer.flip()
    } catch (t: Throwable) {
        MemoryUtil.memFree(buffer)
        throw t
    }

    return DisplayList(buffer, textures.toIntArray(), vertexCounts.toIntArray())
}
