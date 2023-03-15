package io.github.gaming32.kiloengine

import io.github.gaming32.kiloengine.entity.BaseComponent
import io.github.gaming32.kiloengine.entity.Entity
import io.github.gaming32.kiloengine.util.cast
import io.github.gaming32.kiloengine.util.wrapperType
import org.joml.Vector3f
import org.ode4j.ode.DBody
import org.ode4j.ode.DSpace
import org.ode4j.ode.DWorld
import org.ode4j.ode.OdeHelper
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodType
import java.util.function.Consumer

class Scene {
    val world: DWorld = OdeHelper.createWorld()
    val space: DSpace = OdeHelper.createSimpleSpace()

    @PublishedApi
    internal val bodyToEntity = mutableMapOf<DBody, Entity>()

    var skybox: SkyboxTextures? = null
    var sunPosition: Vector3f? = null

    private var eventsDirty = false
    private val events = buildMap {
        EventType.EVENT_TYPES.forEach { eventType ->
            put(eventType, mutableListOf<EventInvoker>())
        }
    }

    init {
        world.setGravity(0.0, -11.0, 0.0)
    }

    fun addEntity(entity: Entity) {
        bodyToEntity[entity.body] = entity
        EventType.EVENT_TYPES.forEach { eventType ->
            val eventList = events.getValue(eventType)
            entity.components.forEach { calculateEvent(it, eventType, eventList) }
        }
    }

    fun getEntityByBody(body: DBody) = bodyToEntity[body]

    fun removeEntity(entity: Entity) {
        bodyToEntity.remove(entity.body)
        entity.destroy()
        eventsDirty = true
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

    fun <T : BaseComponent<T>> getComponents(type: BaseComponent.ComponentType<T>) =
        entities.asSequence().flatMap { it.getComponents(type) }

    inline fun <reified T : BaseComponent<T>> getComponentOrNull() =
        entities.firstNotNullOfOrNull { it.getComponentOrNull<T>() }

    inline fun <reified T : BaseComponent<T>> getComponent() = getComponentOrNull<T>()
        ?: throw IllegalArgumentException("$this missing entity of type ${T::class.java.simpleName}")

    inline fun <reified T : BaseComponent<T>> getComponents() =
        entities.asSequence().flatMap { it.getComponents<T>() }

    private fun calculateEventTypes() {
        EventType.EVENT_TYPES.forEach { eventType ->
            val eventList = events.getValue(eventType)
            eventList.clear()
            entities.asSequence().flatMap(Entity::components).forEach { calculateEvent(it, eventType, eventList) }
        }
        eventsDirty = false
    }

    internal fun calculateEvents(component: BaseComponent<*>) {
        EventType.EVENT_TYPES.forEach { calculateEvent(component, it, events.getValue(it)) }
    }

    private fun calculateEvent(
        component: BaseComponent<*>,
        eventType: EventType<*>,
        eventList: MutableList<EventInvoker>
    ) {
        var type: Class<in BaseComponent<*>> = component.javaClass
        while (type != BaseComponent::class.java) {
            val callable: EventInvoker? = try {
                if (eventType.argType == null) {
                    val method = component.javaClass.getDeclaredMethod(eventType.name)
                    val lambda = LambdaMetafactory.metafactory(
                        BaseComponent.LOOKUP,
                        "run",
                        MethodType.methodType(Runnable::class.java, type),
                        MethodType.methodType(Nothing::class.javaPrimitiveType),
                        BaseComponent.LOOKUP.unreflect(method),
                        MethodType.methodType(Nothing::class.javaPrimitiveType)
                    ).target(component) as Runnable
                    { _ -> lambda.run() }
                } else {
                    val method = component.javaClass.getDeclaredMethod(eventType.name, eventType.argType)
                    LambdaMetafactory.metafactory(
                        BaseComponent.LOOKUP,
                        "accept",
                        MethodType.methodType(Consumer::class.java, type),
                        MethodType.methodType(Nothing::class.javaPrimitiveType, Any::class.java),
                        BaseComponent.LOOKUP.unreflect(method),
                        MethodType.methodType(Nothing::class.javaPrimitiveType, eventType.argType.wrapperType)
                    ).target(component).cast<Consumer<Any?>>()::accept
                }
            } catch (_: NoSuchMethodException) {
                null
            }
            if (callable != null) {
                eventList += callable
                break
            }
            type = type.superclass
        }
    }

    internal fun <T> invokeEvent(eventType: EventType<T>, arg: T) {
        if (eventsDirty) {
            calculateEventTypes()
        }
        events.getValue(eventType).forEach { it(arg) }
    }

    internal fun invokeEvent(eventType: EventType<Nothing?>) {
        if (eventsDirty) {
            calculateEventTypes()
        }
        events.getValue(eventType).forEach { it(null) }
    }
}
