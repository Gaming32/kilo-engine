package io.github.gaming32.kiloengine.mesh

import org.ode4j.ode.DTriMeshData
import org.ode4j.ode.OdeHelper

data class CollisionModel(
    val mesh: Mesh<*>,
    val collisionTypes: Map<Material, CollisionType>
) {
    companion object {
        @Suppress("unused")
        val EMPTY = CollisionModel(Model.EMPTY, mapOf())
    }

    fun getCollision(mat: Material?) = collisionTypes[mat] ?: CollisionTypes.SOLID

    fun getCollision(triangle: Mesh.Triangle) = getCollision(triangle.material)

    fun toTriMeshData(
        collisionType: CollisionType,
        data: DTriMeshData = OdeHelper.createTriMeshData()
    ) = data.also {
        val vertices = mutableMapOf<Mesh.Vertex, Int>() // ORDERED!
        val indexData = IntArray(mesh.getTriangles().count { getCollision(it) == collisionType } * 3)
        var currentIndex = 0
        mesh.getTriangles().forEach { tri ->
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
        for (tri in mesh.getTriangles()) {
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
