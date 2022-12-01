package io.github.gaming32.fungame.entity

import com.google.gson.JsonObject
import io.github.gaming32.fungame.loader.LevelLoader
import io.github.gaming32.fungame.util.getElement
import io.github.gaming32.fungame.util.toDMatrix3
import org.joml.Math
import org.joml.Matrix3d
import org.ode4j.ode.OdeHelper

class CapsuleColliderComponent(
    entity: Entity, radius: Double, length: Double
) : BaseComponent<CapsuleColliderComponent>(Type, entity) {
    companion object Type : ComponentType<CapsuleColliderComponent>() {
        val Z_FORWARD = Matrix3d().rotateX(Math.PI / 2).toDMatrix3()

        override fun create(entity: Entity, loader: LevelLoader, data: JsonObject) =
            CapsuleColliderComponent(entity, data.getElement("radius").asDouble, data.getElement("length").asDouble)
    }

    private val geom = OdeHelper.createCapsule(entity.level.space, radius, length)

    init {
        geom.body = entity.body
        geom.offsetRotation = Z_FORWARD
    }

    override fun destroy() = geom.destroy()
}
