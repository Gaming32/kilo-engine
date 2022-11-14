package io.github.gaming32.fungame.model

import io.github.gaming32.fungame.util.Drawable
import io.github.gaming32.fungame.util.ModelBuilder
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*

data class Model(val tris: List<Tri>) : Drawable {
    data class UV(val u: Float, val v: Float) : Drawable {
        override fun draw(builder: ModelBuilder) {
            builder.uv(u, v)
        }
    }

    data class Vertex(
        val position: Vector3f,
        val normal: Vector3f? = null,
        val uv: UV? = null,
        var collisionCache: Vector3f? = null
    ) : Drawable {
        override fun draw(builder: ModelBuilder) {
            uv?.draw(builder)
            normal?.let { builder.normal(it) }
            builder.vertex(position)
        }
    }

    data class Tri(val a: Vertex, val b: Vertex, val c: Vertex, val material: Material? = null) : Drawable {
        fun draw(builder: ModelBuilder, hasCollision: Boolean) {
            if (hasCollision) {
                builder.disable(GL_TEXTURE_2D)
                builder.color(0f, 0f, 1f, 1f)
                builder.begin(GL_LINES)
                builder.vertex(a.position)
                builder.vertex(Vector3f(a.normal).add(a.position))
                builder.draw()
            } else {
                material?.draw(builder)
            }
            builder.begin(GL_TRIANGLES)
            a.draw(builder)
            b.draw(builder)
            c.draw(builder)
            builder.draw()
        }

        override fun draw(builder: ModelBuilder) = draw(builder, false)
    }

    fun draw(builder: ModelBuilder, collisions: List<Tri>) {
        tris.forEach {
            it.draw(builder, it in collisions)
        }
    }

    override fun draw(builder: ModelBuilder) = draw(builder, listOf())
}
