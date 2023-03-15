package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.loader.SceneLoader
import io.github.gaming32.kiloengine.model.Model
import io.github.gaming32.kiloengine.util.getElement

class MeshComponent(entity: Entity, val model: Model) : BaseComponent<MeshComponent>(Type, entity) {
    companion object Type : ComponentType<MeshComponent>() {
        override fun create(entity: Entity, loader: SceneLoader, data: JsonObject) = MeshComponent(
            entity,
            loader.loadObj(data.getElement("mesh").asString).run {
                data["scale"]?.asFloat?.let { scale(it) } ?: this
            }
        )
    }

    override fun destroy() = Unit
}
