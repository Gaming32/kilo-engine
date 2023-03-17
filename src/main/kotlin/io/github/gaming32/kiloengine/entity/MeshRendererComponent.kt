package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.MatrixStacks
import io.github.gaming32.kiloengine.loader.SceneLoader

class MeshRendererComponent(entity: Entity) : BaseComponent<MeshRendererComponent>(Type, entity) {
    companion object Type : ComponentType<MeshRendererComponent>() {
        override fun create(entity: Entity, loader: SceneLoader, data: JsonObject) =
            MeshRendererComponent(entity)
    }

    private val displayList = entity.getComponent<MeshComponent>().model.toDisplayList()

    override fun destroy() = displayList.destroy()

    override fun draw(matrices: MatrixStacks) = drawPositioned(matrices) {
        displayList.draw(matrices)
    }
}
