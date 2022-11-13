package io.github.gaming32.fungame.model

import io.github.gaming32.fungame.util.Drawable
import io.github.gaming32.fungame.util.ModelBuilder
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.GL_TRIANGLES

data class Model(val tris: List<Tri>) : Drawable {
    companion object {
        private val COLLISION_MATERIAL = Material.Color(0f, 0f, 1f, 1f)
    }

    data class UV(val u: Float, val v: Float) : Drawable {
        override fun draw(builder: ModelBuilder) {
            builder.uv(u, v)
        }
    }

    data class Vertex(val position: Vector3f, val normal: Vector3f? = null, val uv: UV? = null) : Drawable {
        override fun draw(builder: ModelBuilder) {
            uv?.draw(builder)
            normal?.let { builder.normal(it) }
            builder.vertex(position)
        }
    }

    data class Tri(val a: Vertex, val b: Vertex, val c: Vertex, val material: Material? = null) : Drawable {
        fun draw(builder: ModelBuilder, overrideMaterial: Material?) {
            (overrideMaterial ?: material)?.draw(builder)
            builder.begin(GL_TRIANGLES)
            a.draw(builder)
            b.draw(builder)
            c.draw(builder)
            builder.draw()
        }

        override fun draw(builder: ModelBuilder) = draw(builder, null)
    }

    fun draw(builder: ModelBuilder, collisions: List<Tri>) {
        tris.forEach {
            it.draw(builder, if (it in collisions) COLLISION_MATERIAL else null)
        }
    }

    override fun draw(builder: ModelBuilder) = draw(builder, listOf())
}
