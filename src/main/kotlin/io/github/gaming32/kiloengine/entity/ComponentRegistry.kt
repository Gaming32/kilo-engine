package io.github.gaming32.kiloengine.entity

import io.github.gaming32.kiloengine.entity.BaseComponent.ComponentType

object ComponentRegistry {
    private val componentTypes = mutableMapOf<String, ComponentType<*>>()

    fun register(id: String, type: ComponentType<*>) {
        componentTypes[id] = type
    }

    fun getType(id: String) = componentTypes.getValue(id)

    fun identifierOf(type : ComponentType<*>) : String {
        componentTypes.forEach {
            if (it.value == type) return it.key
        }
        return "unknownComponent"
    }

    init {
        register("camera", CameraComponent)
        register("capsuleCollider", CapsuleColliderComponent)
        register("mesh", MeshComponent)
        register("meshCollider", MeshColliderComponent)
        register("meshRenderer", MeshRendererComponent)
        register("player", PlayerComponent)
    }
}
