package io.github.gaming32.fungame

import io.github.gaming32.fungame.entity.Entity
import io.github.gaming32.fungame.entity.EntityType
import org.ode4j.ode.*

class Level {
    val world: DWorld = OdeHelper.createWorld()
    val space: DSpace = OdeHelper.createSimpleSpace()

    @PublishedApi
    internal val bodyToEntity = mutableMapOf<DBody, Entity<*>>()

    init {
        world.setGravity(0.0, -11.0, 0.0)
    }

    fun addEntity(entity: Entity<*>) {
        bodyToEntity[entity.body] = entity
    }

    fun getEntityByBody(body: DBody) = bodyToEntity[body]

    fun removeEntity(entity: Entity<*>) {
        bodyToEntity.remove(entity.body)
        entity.destroy()
    }

    fun destroy() {
        forEachEntity(Entity<*>::destroy)
        world.destroy()
        space.destroy()
    }

    val entities get() = bodyToEntity.values

    inline fun forEachEntity(action: (Entity<*>) -> Unit) = bodyToEntity.values.forEach(action)

    @Suppress("UNCHECKED_CAST")
    fun <T : Entity<T>> getEntityOfType(type: EntityType<T>): T = bodyToEntity.values.first { it.type == type } as T
}
