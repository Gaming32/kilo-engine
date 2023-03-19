package io.github.gaming32.kiloengine.model

object MeshRegistry {
    private val meshes = mutableMapOf<String, Mesh>()

    @JvmStatic
    @Suppress("unused")
    fun register(id: String, mesh: Mesh) {
        meshes[id] = mesh
    }

    @JvmStatic
    operator fun get(id: String) = meshes.getValue(id)
}