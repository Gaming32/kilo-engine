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

    data class Vertex(
        val position: Vector3f,
        val normal: Vector3f? = null,
        val uv: UV? = null
    ) : Drawable {
        override fun draw(builder: ModelBuilder) {
            uv?.draw(builder)
            normal?.let { builder.normal(it) }
            builder.vertex(position)
        }
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
    }

    companion object {
        val EMPTY = Model(listOf(), mapOf())
    }

    override fun draw(builder: ModelBuilder) {
        tris.forEach {
            it.draw(builder)
        }
    }
}
