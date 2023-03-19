package io.github.gaming32.kiloengine.model

import io.github.gaming32.kiloengine.util.invertInto
import java.util.*

/**
 * Represents a mesh with constant
 */
data class Model(val triangles: List<Mesh.Triangle>, val materials: Map<String, Material>) : Mesh {
    companion object {
        val EMPTY = Model(listOf(), mapOf())
    }

    override fun getTriangles() = triangles
    override fun getMaterials() = materials

    fun scale(scale: Float) = copy(triangles = triangles.map { it.scale(scale) })

    fun replaceMaterials(newMaterials: Map<String, Material>): Model {
        val originals = materials.invertInto(IdentityHashMap())
        return Model(
            triangles.map { it.copy(material = newMaterials[originals[it.material]] ?: it.material) },
            materials + newMaterials
        )
    }
}
