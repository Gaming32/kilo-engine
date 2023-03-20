package io.github.gaming32.kiloengine.model

import java.io.Serializable

/**
 * Represents a non-procedural mesh. Contains useful methods that do not apply for non-static meshes.
 */
interface StaticMesh<T : StaticMesh<T>> : Mesh, Serializable {
    fun scale(scale: Float) : T
    fun replaceMaterials(newMaterials: Map<String, Material>): T
}