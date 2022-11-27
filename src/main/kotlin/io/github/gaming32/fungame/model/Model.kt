package io.github.gaming32.fungame.model

import io.github.gaming32.fungame.util.Drawable
import io.github.gaming32.fungame.util.ModelBuilder
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.ode4j.ode.DTriMeshData
import org.ode4j.ode.OdeHelper

data class Model(val tris: List<Tri>) : Drawable {
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

    fun toTriMeshData(data: DTriMeshData = OdeHelper.createTriMeshData()) = data.also {
        val vertices = mutableMapOf<Vertex, Int>() // ORDERED!
        val indexData = IntArray(tris.size * 3)
        tris.forEachIndexed { index, tri ->
            indexData[index * 3] = vertices.computeIfAbsent(tri.a) { vertices.size }
            indexData[index * 3 + 1] = vertices.computeIfAbsent(tri.b) { vertices.size }
            indexData[index * 3 + 2] = vertices.computeIfAbsent(tri.c) { vertices.size }
        }
        val vertexData = FloatArray(vertices.size * 3)
        vertices.keys.forEachIndexed { index, vertex ->
            vertexData[index * 3] = vertex.position.x
            vertexData[index * 3 + 1] = vertex.position.y
            vertexData[index * 3 + 2] = vertex.position.z
        }
        data.build(vertexData, indexData)
    }
}
