package io.github.gaming32.fungame.util

import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*

class ModelBuilder {
    fun begin(mode: Int) = apply { glBegin(mode) }

    fun vertex(x: Float, y: Float) = apply { glVertex2f(x, y) }

    fun vertex(v: Vector2f) = vertex(v.x, v.y)

    fun vertex(x: Float, y: Float, z: Float) = apply { glVertex3f(x, y, z) }

    fun vertex(v: Vector3f) = vertex(v.x, v.y, v.z)

    fun uv(u: Float, v: Float) = apply { glTexCoord2f(u, v) }

    fun uv(v: Vector2f) = uv(v.x, v.y)

    fun color(r: Float, g: Float, b: Float) = apply { glColor3f(r, g, b) }

    fun color(r: Float, g: Float, b: Float, a: Float) = apply { glColor4f(r, g, b, a) }

    fun disable(target: Int) = apply { glDisable(target) }

    fun enable(target: Int) = apply { glEnable(target) }

    fun texture(name: String) = apply { glBindTexture(GL_TEXTURE_2D, TextureManager.getTexture(name)) }

    fun draw() = apply { glEnd() }

    inline fun draw(mode: Int, action: ModelBuilder.() -> Unit) {
        begin(mode)
        action()
        draw()
    }
}

inline fun buildModel(builder: ModelBuilder.() -> Unit) = ModelBuilder().builder()

inline fun drawModel(mode: Int, action: ModelBuilder.() -> Unit) = ModelBuilder().draw(mode, action)
