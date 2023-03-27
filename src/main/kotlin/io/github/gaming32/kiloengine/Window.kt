package io.github.gaming32.kiloengine

import org.joml.Vector2i
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*

sealed class Window private constructor(internal val lwjglID: Long) {
    internal fun makeCurrent() = glfwMakeContextCurrent(lwjglID)
    internal fun shouldClose() = glfwWindowShouldClose(lwjglID)
    internal fun swapBuffers() = glfwSwapBuffers(lwjglID)
    internal fun freeCallbacks() = glfwFreeCallbacks(lwjglID)
    internal fun destroy() = glfwDestroyWindow(lwjglID)


    internal fun getFramebufferHeight() : Int {
        val height = IntArray(1)
        glfwGetFramebufferSize(lwjglID, null, height)

        return height[0]
    }

    internal fun getFramebufferWidth() : Int {
        val width = IntArray(1)
        glfwGetFramebufferSize(lwjglID, width, null)

        return width[0]
    }

    internal fun getFramebufferSize() : Vector2i {
        val width = IntArray(1)
        val height = IntArray(1)
        glfwGetFramebufferSize(lwjglID, width, height)

        return Vector2i(width[0], height[0])
    }

    class KnownSize internal constructor(lwjglID: Long, val size: Vector2i) : Window(lwjglID) {
        val pixelRatio: Float = getFramebufferWidth().toFloat() / size.x

        internal constructor(size: Vector2i, title: String, monitor: Long = 0L, window: Window? = null) : this(
            glfwCreateWindow(
                size.x, size.y, title, monitor,
                window?.lwjglID ?: 0L
            ), size
        )
    }

    class UnknownSize internal constructor(lwjglID: Long) : Window(lwjglID)
}