package io.github.gaming32.kiloengine

import org.joml.Vector3d

internal typealias EventInvoker = (Any?) -> Unit

internal class EventType<T>(
    val name: String,
    val argType: Class<T>? = null
) {
    companion object {
        val PRE_TICK = EventType<Nothing?>("preTick")
        val TICK = EventType<Nothing?>("tick")
        val DRAW = EventType<Nothing?>("draw")
        val HANDLE_MOVEMENT = EventType("handleMovement", Vector3d::class.java)
        val DRAW_UI = EventType("drawUi", Long::class.javaPrimitiveType)
        val MOUSE_MOVED = EventType("mouseMoved", MouseMoveEvent::class.java)
        val EVENT_TYPES = mutableListOf(PRE_TICK, TICK, DRAW, HANDLE_MOVEMENT, DRAW_UI, MOUSE_MOVED)
    }

    override fun toString() = buildString {
        append(name)
        append('(')
        argType?.simpleName?.let(::append)
        append(')')
    }
}
