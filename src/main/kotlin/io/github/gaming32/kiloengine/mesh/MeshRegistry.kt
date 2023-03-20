package io.github.gaming32.kiloengine.mesh

import io.github.gaming32.kiloengine.mesh.Mesh.MeshType

object MeshRegistry {
    private val meshes = mutableMapOf<String, MeshType<*>>()

    @JvmStatic
    @Suppress("unused")
    fun register(identifier: String, mesh: MeshType<*>) {
        meshes[identifier] = mesh
    }

    @JvmStatic
    operator fun get(identifier: String) = meshes.getValue(identifier)
}