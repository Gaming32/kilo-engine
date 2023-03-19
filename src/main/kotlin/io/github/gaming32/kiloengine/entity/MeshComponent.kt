package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.loader.SceneLoader
import io.github.gaming32.kiloengine.model.Mesh
import io.github.gaming32.kiloengine.util.getElement

class MeshComponent(entity: Entity, val mesh: Mesh) : BaseComponent<MeshComponent>(Type, entity) {
    companion object Type : ComponentType<MeshComponent>() {
        override fun create(entity: Entity, loader: SceneLoader, data: JsonObject) = MeshComponent(
            entity,
            loader.loadOBJ(data.getElement("mesh").asString).run {
                data["scale"]?.asFloat?.let { scale(it) } ?: this
            }
        )
    }

    override fun destroy() = Unit
}
