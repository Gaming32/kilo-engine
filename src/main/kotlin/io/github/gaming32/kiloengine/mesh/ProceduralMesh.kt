package io.github.gaming32.kiloengine.mesh

import io.github.gaming32.kiloengine.util.Dirtable

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

    final override fun markDirty() {
        dirty = true
    }
    final override fun isDirty(): Boolean = dirty
    final override fun getTriangles(): List<Mesh.Triangle> = triangles
}