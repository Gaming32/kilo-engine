package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.loader.LevelLoader
import io.github.gaming32.kiloengine.model.CollisionModel
import io.github.gaming32.kiloengine.model.CollisionType
import io.github.gaming32.kiloengine.util.getElement
import org.ode4j.ode.DContact
import org.ode4j.ode.DContactGeom
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

class MeshColliderComponent(
    entity: Entity, val model: CollisionModel
) : BaseComponent<MeshColliderComponent>(Type, entity) {
    companion object Type : ComponentType<MeshColliderComponent>() {
        override fun create(entity: Entity, loader: LevelLoader, data: JsonObject) = MeshColliderComponent(
            entity,
            loader.loadCollision(
                entity.getComponent<MeshComponent>().model,
                data.getElement("collision").asString
            )
        )
    }

    private val collisionMeshes: Map<DGeom, CollisionType> =
        model.toMultiTriMeshData().entries.associate { (collision, mesh) ->
            val geom = OdeHelper.createTriMesh(
                entity.level.space, mesh,
                { _, _, _ -> 1 },
                { _, _, _, _ -> },
                { _, _, _, _, _ -> 1 }
            )
            geom.body = entity.body
            geom to collision
        }

    override fun destroy() = collisionMeshes.keys.forEach(DGeom::destroy)

    override fun collideWithEntity(
        other: Entity,
        contact: DContactGeom,
        selfIsG1: Boolean
    ): DContact.DSurfaceParameters? = other.collideWithMesh(
        collisionMeshes.getValue(if (selfIsG1) contact.g1 else contact.g2),
        contact, !selfIsG1
    )
}
