package io.github.gaming32.kiloengine.mesh

object MeshRegistry {
    private val meshes = mutableMapOf<String, Mesh>()

    @JvmStatic
    @Suppress("unused")
    fun register(identifier: String, mesh: Mesh) {
        meshes[identifier] = mesh
    }

    @JvmStatic
    operator fun get(identifier: String) = meshes.getValue(identifier)
}