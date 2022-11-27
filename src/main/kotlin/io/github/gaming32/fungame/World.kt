package io.github.gaming32.fungame

import io.github.gaming32.fungame.entity.Entity
import org.ode4j.ode.DGeom
import org.ode4j.ode.DSpace
import org.ode4j.ode.DWorld
import org.ode4j.ode.OdeHelper

class World {
    val world: DWorld = OdeHelper.createWorld()
    val space: DSpace = OdeHelper.createSimpleSpace()

    @PublishedApi
    internal val geomToEntity = mutableMapOf<DGeom, Entity>()

    init {
        world.setGravity(0.0, -11.0, 0.0)
        world.setDamping(0.0, 1.0)
    }

    fun addEntity(entity: Entity) {
        geomToEntity[entity.geom] = entity
    }

    fun getEntityByGeom(geom: DGeom) =
        geomToEntity[geom]
            ?: throw IllegalArgumentException("No entity exists from geom $geom")

    fun removeEntity(entity: Entity) {
        geomToEntity.remove(entity.geom)
        entity.geom.destroy()
        entity.body.destroy()
    }

    fun destroy() {
        world.destroy()
        space.destroy()
    }

    inline fun forEachEntity(action: (Entity) -> Unit) = geomToEntity.values.forEach(action)
}
