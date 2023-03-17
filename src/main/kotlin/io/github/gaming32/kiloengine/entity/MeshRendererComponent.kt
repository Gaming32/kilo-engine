package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.MatrixStacks
import io.github.gaming32.kiloengine.loader.SceneLoader
import io.github.gaming32.kiloengine.util.x
import io.github.gaming32.kiloengine.util.y
import io.github.gaming32.kiloengine.util.z

class MeshRendererComponent(entity: Entity) : BaseComponent<MeshRendererComponent>(Type, entity) {
    companion object Type : ComponentType<MeshRendererComponent>() {
        override fun create(entity: Entity, loader: SceneLoader, data: JsonObject) =
            MeshRendererComponent(entity)
    }

    private val displayList = entity.getComponent<MeshComponent>().model.toDisplayList()

    override fun destroy() = displayList.destroy()

    override fun draw(matrices: MatrixStacks) {
        matrices.model.pushMatrix()
        val position = entity.body.position
        matrices.model.translate(position.x.toFloat(), position.y.toFloat(), position.z.toFloat())
        displayList.draw(matrices)
        matrices.model.popMatrix()
    }
}
