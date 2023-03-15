package io.github.gaming32.kiloengine.model

import io.github.gaming32.kiloengine.util.Drawable
import io.github.gaming32.kiloengine.util.ModelBuilder
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.GL_TRIANGLES

data class Model(val tris: List<Tri>, val materials: Map<String, Material>) : Drawable {
    data class UV(val u: Float, val v: Float) : Drawable {
        override fun draw(builder: ModelBuilder) {
            builder.uv(u, v)
        }
    }

    data class Vertex @JvmOverloads constructor(
        val position: Vector3f,
        val normal: Vector3f? = null,
        val uv: UV? = null,
        val color: Material.Color = Material.Color.DEFAULT
    ) : Drawable {
        override fun draw(builder: ModelBuilder) {
            uv?.draw(builder)
            normal?.let { builder.normal(it) }
            builder.color(color.r, color.g, color.b)
            builder.vertex(position)
        }

        fun scale(scale: Float) = copy(position = Vector3f(position).mul(scale))
    }

    data class Tri(val a: Vertex, val b: Vertex, val c: Vertex, val material: Material? = null) : Drawable {
        override fun draw(builder: ModelBuilder) {
            if (material?.isFullyTransparent == true) return
            material?.draw(builder)
            builder.begin(GL_TRIANGLES)
            a.draw(builder)
            b.draw(builder)
            c.draw(builder)
            builder.draw()
        }

        fun scale(scale: Float) = copy(a = a.scale(scale), b = b.scale(scale), c = c.scale(scale))
    }

    companion object {
        val EMPTY = Model(listOf(), mapOf())
    }

    override fun draw(builder: ModelBuilder) {
        tris.forEach {
            it.draw(builder)
        }
    }

    fun scale(scale: Float) = copy(tris = tris.map { it.scale(scale) })
}
