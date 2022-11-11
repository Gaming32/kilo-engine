package io.github.gaming32.fungame.model

import io.github.gaming32.fungame.util.Drawable
import io.github.gaming32.fungame.util.ModelBuilder
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.GL_TRIANGLES

data class Model(val tris: List<Tri>) : Drawable {
    data class UV(val u: Float, val v: Float) : Drawable {
        override fun draw(builder: ModelBuilder) {
            builder.uv(u, v)
        }
    }

    data class Vertex(val x: Float, val y: Float, val z: Float, val uv: UV? = null) : Drawable {
        constructor(position: Vector3f, uv: UV? = null) : this(position.x, position.y, position.z, uv)

        override fun draw(builder: ModelBuilder) {
            uv?.draw(builder)
            builder.vertex(x, y, z)
        }
    }

    data class Tri(val a: Vertex, val b: Vertex, val c: Vertex, val material: Material? = null) : Drawable {
        override fun draw(builder: ModelBuilder) {
            material?.draw(builder)
            builder.begin(GL_TRIANGLES)
            a.draw(builder)
            b.draw(builder)
            c.draw(builder)
            builder.draw()
        }
    }

    override fun draw(builder: ModelBuilder) {
        tris.forEach {
            it.draw(builder)
        }
    }
}
