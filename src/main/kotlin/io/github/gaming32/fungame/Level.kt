package io.github.gaming32.fungame

import io.github.gaming32.fungame.entity.Entity
import io.github.gaming32.fungame.entity.EntityType
import io.github.gaming32.fungame.model.CollisionModel
import io.github.gaming32.fungame.model.CollisionType
import org.ode4j.ode.*

class Level {
    val world: DWorld = OdeHelper.createWorld()
    val space: DSpace = OdeHelper.createSimpleSpace()

    @PublishedApi
    internal val geomToEntity = mutableMapOf<DGeom, Entity<*>>()

    val levelBody: DBody = OdeHelper.createBody(world).also { it.setKinematic() }
    var levelModel = CollisionModel.EMPTY
        private set
    private val collisionMeshes = mutableMapOf<DGeom, CollisionType>()

    init {
        world.setGravity(0.0, -11.0, 0.0)
    }

    fun setGeom(model: CollisionModel) {
        levelModel = model
        collisionMeshes.clear()
        for ((collision, mesh) in model.toMultiTriMeshData()) {
            val geom = OdeHelper.createTriMesh(
                space, mesh,
                { _, _, _ -> 1 },
                { _, _, _, _ -> },
                { _, _, _, _, _ -> 1 }
            )
            collisionMeshes[geom] = collision
            geom.body = levelBody
        }
    }

    fun addEntity(entity: Entity<*>) {
        geomToEntity[entity.geom] = entity
    }

    fun getCollisionType(geom: DGeom) = collisionMeshes.getValue(geom)

    fun getEntityByGeom(geom: DGeom) =
        geomToEntity[geom]
            ?: throw IllegalArgumentException("No entity exists from geom $geom")

    fun removeEntity(entity: Entity<*>) {
        geomToEntity.remove(entity.geom)
        entity.geom.destroy()
        entity.body.destroy()
    }

    fun destroy() {
        world.destroy()
        space.destroy()
    }

    inline fun forEachEntity(action: (Entity<*>) -> Unit) = geomToEntity.values.forEach(action)

    @Suppress("UNCHECKED_CAST")
    fun <T : Entity<T>> getEntityOfType(type: EntityType<T>): T = geomToEntity.values.first { it.type == type } as T
}
