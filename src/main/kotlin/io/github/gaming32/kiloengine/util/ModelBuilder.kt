@file:Suppress("unused")

package io.github.gaming32.kiloengine.util

import io.github.gaming32.kiloengine.TextureManager
import org.joml.Vector2f
import org.joml.Vector3f
import java.nio.FloatBuffer

class ModelBuilder {
    data class ModelVertex(
        val position: Vector3f = Vector3f(),
        val normal: Vector3f = Vector3f(),
        val uv: Vector2f = Vector2f(),
        val color: Vector3f = Vector3f(1f, 1f, 1f),
        var texture: Int = 0
    ) {
        fun store(buffer: FloatBuffer) {
            val pos = buffer.position()
            position.get(pos, buffer)
            normal.get(pos + 3, buffer)
            uv.get(pos + 6, buffer)
            color.get(pos + 8, buffer)
            buffer.position(pos + 11)
        }
    }

    val elements = mutableListOf<ModelVertex>()

    init {
        elements += ModelVertex()
    }

    fun position(x: Float, y: Float) = position(x, y, 0f)

    fun position(v: Vector2f) = position(v.x, v.y, 0f)

    fun position(x: Float, y: Float, z: Float) = apply { elements.last().position.set(x, y, z) }

    fun position(v: Vector3f) = apply { elements.last().position.set(v) }

    fun normal(x: Float, y: Float, z: Float) = apply { elements.last().normal.set(x, y, z) }

    fun normal(v: Vector3f) = apply { elements.last().normal.set(v) }

    fun uv(u: Float, v: Float) = apply { elements.last().uv.set(u, v) }

    fun uv(v: Vector2f) = apply { elements.last().uv.set(v) }

    fun color(r: Float, g: Float, b: Float) = apply { elements.last().color.set(r, g, b) }

    fun color(v: Vector3f) = apply { elements.last().color.set(v) }

    fun color(r: Float, g: Float, b: Float, a: Float) = apply {
        if (a == 1f) {
            color(r, g, b)
        } else {
            throw UnsupportedOperationException("Alpha not supported yet")
        }
    }

    fun texture(name: String) = apply {
        elements.last().texture = TextureManager.getTexture(name)
    }

    fun next() = apply {
        // Inherit the texture from the previous vertex
        elements += ModelVertex(texture = elements.last().texture)
    }
}
