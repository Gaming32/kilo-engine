package io.github.gaming32.kiloengine.mesh

import io.github.gaming32.kiloengine.util.invertInto
import java.util.*

/**
 * Represents a mesh with never changing triangles.
 */
data class Model(private val triangles: List<Mesh.Triangle>, val materials: Map<String, Material>) : StaticMesh<Model> {
    companion object {
        val EMPTY = Model(listOf(), mapOf())
    }

    override fun getTriangles() = triangles
    override fun getMaterial(key: String) = materials[key]

    override fun scale(scale: Float) = copy(triangles = triangles.map { it.scale(scale) })

    override fun replaceMaterials(newMaterials: Map<String, Material>): Model {
        val originals = materials.invertInto(IdentityHashMap())
        return Model(
            triangles.map { it.copy(material = newMaterials[originals[it.material]] ?: it.material) },
            materials + newMaterials
        )
    }
}
