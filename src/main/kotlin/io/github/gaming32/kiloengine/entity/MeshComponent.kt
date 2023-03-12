package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.loader.LevelLoader
import io.github.gaming32.kiloengine.model.Model
import io.github.gaming32.kiloengine.util.getElement

class MeshComponent(entity: Entity, val model: Model) : BaseComponent<MeshComponent>(Type, entity) {
    companion object Type : ComponentType<MeshComponent>() {
        override fun create(entity: Entity, loader: LevelLoader, data: JsonObject) =
            MeshComponent(entity, loader.loadObj(data.getElement("mesh").asString))
    }

    override fun destroy() = Unit
}
