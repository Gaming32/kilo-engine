package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.loader.SceneLoader
import io.github.gaming32.kiloengine.mesh.Mesh
import io.github.gaming32.kiloengine.mesh.MeshRegistry
import io.github.gaming32.kiloengine.mesh.ProceduralMesh
import io.github.gaming32.kiloengine.util.getElement

class MeshComponent(entity: Entity, val mesh: Mesh<*>) : BaseComponent<MeshComponent>(Type, entity) {
    companion object Type : ComponentType<MeshComponent>() {
        override fun create(entity: Entity, loader: SceneLoader, data: JsonObject) = MeshComponent(
            entity,
            if (data.has("fromRegistry") &&
                data.getElement("fromRegistry").let { it.isJsonPrimitive && it.asBoolean }) {
                MeshRegistry[data.getElement("mesh").asString].create(data)
            } else {
                loader.loadOBJ(data.getElement("mesh").asString).run {
                    data["scale"]?.asFloat?.let { scale(it) } ?: this
                }
            }
        )
    }

    override fun tick() {
        if (mesh is ProceduralMesh)
            mesh.recalculateMeshIfDirty()
    }

    override fun destroy() = Unit
}
