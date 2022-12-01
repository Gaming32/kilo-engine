package io.github.gaming32.fungame

import io.github.gaming32.fungame.entity.BaseComponent
import io.github.gaming32.fungame.entity.Entity
import org.ode4j.ode.DBody
import org.ode4j.ode.DSpace
import org.ode4j.ode.DWorld
import org.ode4j.ode.OdeHelper

class Level {
    val world: DWorld = OdeHelper.createWorld()
    val space: DSpace = OdeHelper.createSimpleSpace()

    @PublishedApi
    internal val bodyToEntity = mutableMapOf<DBody, Entity>()

    init {
        world.setGravity(0.0, -11.0, 0.0)
    }

    fun addEntity(entity: Entity) {
        bodyToEntity[entity.body] = entity
    }

    fun getEntityByBody(body: DBody) = bodyToEntity[body]

    fun removeEntity(entity: Entity) {
        bodyToEntity.remove(entity.body)
        entity.destroy()
    }

    fun destroy() {
        entities.forEach(Entity::destroy)
        world.destroy()
        space.destroy()
    }

    val entities get() = bodyToEntity.values

    fun <T : BaseComponent<T>> getComponentOrNull(type: BaseComponent.ComponentType<T>) =
        entities.firstNotNullOfOrNull { it.getComponentOrNull(type) }

    fun <T : BaseComponent<T>> getComponent(type: BaseComponent.ComponentType<T>) =
        getComponentOrNull(type) ?: throw IllegalArgumentException("$this missing entity of type $type")

    inline fun <reified T : BaseComponent<T>> getComponentOrNull() =
        entities.firstNotNullOfOrNull { it.getComponentOrNull<T>() }

    inline fun <reified T : BaseComponent<T>> getComponent() = getComponentOrNull<T>()
        ?: throw IllegalArgumentException("$this missing entity of type ${T::class.java.simpleName}")
}
