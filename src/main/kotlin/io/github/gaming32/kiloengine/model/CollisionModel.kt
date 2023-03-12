package io.github.gaming32.kiloengine.model

import org.ode4j.ode.DTriMeshData
import org.ode4j.ode.OdeHelper

data class CollisionModel(
    val model: Model,
    val collisionTypes: Map<Material, CollisionType>
) {
    companion object {
        val EMPTY = CollisionModel(Model.EMPTY, mapOf())
    }

    fun getCollision(mat: Material?) = collisionTypes[mat] ?: CollisionTypes.SOLID

    fun getCollision(tri: Model.Tri) = getCollision(tri.material)

    fun toTriMeshData(
        collisionType: CollisionType,
        data: DTriMeshData = OdeHelper.createTriMeshData()
    ) = data.also {
        val vertices = mutableMapOf<Model.Vertex, Int>() // ORDERED!
        val indexData = IntArray(model.tris.count { getCollision(it) == collisionType } * 3)
        var currentIndex = 0
        model.tris.forEach { tri ->
            if (getCollision(tri) != collisionType) {
                return@forEach
            }
            indexData[currentIndex++] = vertices.computeIfAbsent(tri.a) { vertices.size }
            indexData[currentIndex++] = vertices.computeIfAbsent(tri.b) { vertices.size }
            indexData[currentIndex++] = vertices.computeIfAbsent(tri.c) { vertices.size }
        }
        val vertexData = FloatArray(vertices.size * 3)
        currentIndex = 0
        vertices.keys.forEach { vertex ->
            vertexData[currentIndex++] = vertex.position.x
            vertexData[currentIndex++] = vertex.position.y
            vertexData[currentIndex++] = vertex.position.z
        }
        data.build(vertexData, indexData)
    }

    fun toMultiTriMeshData(): Map<CollisionType, DTriMeshData> {
        val result = mutableMapOf<CollisionType, DTriMeshData>()
        for (tri in model.tris) {
            val type = getCollision(tri)
            if (
                type in result ||
                type == CollisionTypes.NON_SOLID // Don't even bother checking collisions with non-solid tris
            ) continue
            result[type] = toTriMeshData(type)
        }
        return result
    }
}
