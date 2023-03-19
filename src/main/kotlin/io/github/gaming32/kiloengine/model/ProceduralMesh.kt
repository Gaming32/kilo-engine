package io.github.gaming32.kiloengine.model

import io.github.gaming32.kiloengine.util.Dirtable

/**
 * @see
 */
abstract class ProceduralMesh : Mesh, Dirtable {
    private var triangles: List<Mesh.Triangle>
    private var dirty: Boolean = true

    init {
        @Suppress("LeakingThis")
        triangles = calculateMesh()
    }

    protected abstract fun calculateMesh() : List<Mesh.Triangle>

    fun recalculateMeshIfDirty() {
        if (isDirty()) {
            triangles = calculateMesh()
            dirty = false
        }
    }

    override fun markDirty() {
        dirty = true
    }
    override fun isDirty(): Boolean = dirty
    override fun getTriangles(): List<Mesh.Triangle> = triangles
}