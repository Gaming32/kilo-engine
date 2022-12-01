package io.github.gaming32.fungame.entity

import io.github.gaming32.fungame.Level
import io.github.gaming32.fungame.model.CollisionType
import org.ode4j.math.DVector3C
import org.ode4j.ode.DBody
import org.ode4j.ode.DContact.DSurfaceParameters
import org.ode4j.ode.DContactGeom
import org.ode4j.ode.OdeHelper

class Entity(val level: Level, position: DVector3C) {
    val body: DBody = OdeHelper.createBody(level.world)

    @PublishedApi
    internal val components = mutableListOf<BaseComponent<*>>()

    init {
        body.position = position
        level.addEntity(this)
    }

    fun addComponent(component: BaseComponent<*>) {
        components += component
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseComponent<T>> getComponentOrNull(type: BaseComponent.ComponentType<T>) =
        components.firstOrNull { it.type == type } as T?

    fun <T : BaseComponent<T>> getComponent(type: BaseComponent.ComponentType<T>) =
        getComponentOrNull(type) ?: throw IllegalArgumentException("$this missing component of type $type")

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseComponent<T>> getComponents(type: BaseComponent.ComponentType<T>) =
        components.asSequence().filter { it.type == type }.map { it as T }

    inline fun <reified T : BaseComponent<T>> getComponentOrNull() = components.firstOrNull { it is T } as T?

    inline fun <reified T : BaseComponent<T>> getComponent() = getComponentOrNull<T>()
        ?: throw IllegalArgumentException("$this missing component of type ${T::class.java.simpleName}")

    inline fun <reified T : BaseComponent<T>> getComponents() =
        components.asSequence().filter { it is T }.map { it as T }

    fun collideWithMesh(collision: CollisionType, contact: DContactGeom, selfIsG1: Boolean): DSurfaceParameters? {
        var result: DSurfaceParameters? = null
        for (component in components) {
            val params = component.collideWithMesh(collision, contact, selfIsG1)
            if (result == null) {
                result = params
            }
        }
        return result
    }

    fun collideWithEntity(other: Entity, contact: DContactGeom, selfIsG1: Boolean): DSurfaceParameters? {
        var result: DSurfaceParameters? = null
        for (component in components) {
            val params = component.collideWithEntity(other, contact, selfIsG1)
            if (result == null) {
                result = params
            }
        }
        return result
    }

    fun preTick() = components.forEach(BaseComponent<*>::preTick)

    fun tick() = components.forEach(BaseComponent<*>::tick)

    fun destroy() {
        components.forEach(BaseComponent<*>::destroy)
        body.destroy()
    }

    fun draw() = components.forEach(BaseComponent<*>::draw)
}
