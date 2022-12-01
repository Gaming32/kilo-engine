package io.github.gaming32.fungame.entity

import com.google.gson.JsonObject
import io.github.gaming32.fungame.loader.LevelLoader
import io.github.gaming32.fungame.util.x
import io.github.gaming32.fungame.util.y
import io.github.gaming32.fungame.util.z
import org.lwjgl.opengl.GL11.*

class MeshRendererComponent(entity: Entity) : BaseComponent<MeshRendererComponent>(Type, entity) {
    companion object Type : ComponentType<MeshRendererComponent>() {
        override fun create(entity: Entity, loader: LevelLoader, data: JsonObject) =
            MeshRendererComponent(entity)
    }

    private val displayList = entity.getComponent<MeshComponent>().model.toDisplayList()

    override fun destroy() = displayList.destroy()

    override fun draw() {
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        val position = entity.body.position
        glTranslatef(position.x.toFloat(), position.y.toFloat(), position.z.toFloat())
        displayList.draw()
        glPopMatrix()
    }
}
