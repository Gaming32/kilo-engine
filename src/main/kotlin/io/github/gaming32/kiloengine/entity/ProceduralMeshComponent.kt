package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.loader.SceneLoader
import io.github.gaming32.kiloengine.model.Mesh
import io.github.gaming32.kiloengine.model.MeshRegistry
import io.github.gaming32.kiloengine.model.ProceduralMesh
import io.github.gaming32.kiloengine.util.getElement

/**
 * Uses mesh from the registry instead of from files, useful for procedural meshes as it recalculates them if they're dirty
 */
class ProceduralMeshComponent(entity: Entity, val mesh: Mesh) : BaseComponent<ProceduralMeshComponent>(Type, entity) {
    companion object Type : ComponentType<ProceduralMeshComponent>() {
        override fun create(entity: Entity, loader: SceneLoader, data: JsonObject) = ProceduralMeshComponent(
            entity,
            MeshRegistry[data.getElement("meshIdentifier").asString]
        )
    }

    override fun destroy() = Unit

    override fun tick() {
        if (mesh is ProceduralMesh)
            mesh.recalculateMeshIfDirty()
    }
}