package io.github.gaming32.kiloengine.model

import io.github.gaming32.kiloengine.util.Drawable
import io.github.gaming32.kiloengine.util.ModelBuilder
import org.joml.Vector3f

sealed interface Mesh : Drawable {

    fun getTriangles() : List<Triangle>
    fun getMaterial(key: String) : Material?

    override fun draw(builder: ModelBuilder) {
        getTriangles().forEach {
            it.draw(builder)
        }
    }

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
            builder.position(position)
            builder.next()
        }

        fun scale(scale: Float) = copy(position = Vector3f(position).mul(scale))
    }

    data class Triangle(val a: Vertex, val b: Vertex, val c: Vertex, val material: Material? = null) : Drawable {
        override fun draw(builder: ModelBuilder) {
            if (material?.isFullyTransparent == true) return
            material?.draw(builder)
            a.draw(builder)
            b.draw(builder)
            c.draw(builder)
        }

        fun scale(scale: Float) = copy(a = a.scale(scale), b = b.scale(scale), c = c.scale(scale))
    }
}