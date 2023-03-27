package io.github.gaming32.kiloengine.ui

import io.github.gaming32.kiloengine.Window
import io.github.gaming32.kiloengine.util.loadFont
import org.joml.Vector2fc
import org.lwjgl.nanovg.NanoVG.nvgBeginFrame
import org.lwjgl.nanovg.NanoVG.nvgEndFrame

typealias UITask = (nanovg: Long) -> Unit

/**
 * @param nanovg should not be used directly. Use `.invoke()` instead.
 */
data class UIManager(private val nanovg: Long) {
    private val tasks = mutableListOf<Task>()

    infix fun on(window: Window) {
        nvgBeginFrame(nanovg, window.size.x.toFloat(), window.size.y.toFloat(), window.pixelRatio)

        tasks.removeIf {
            it.task.invoke(nanovg)
            it.removeAfterInvoking
        }

        nvgEndFrame(nanovg)
    }

    fun addTask(task: Task) = tasks.add(task)

    fun addPrioritizedTask(task: Task) = tasks.add(0, task)

    @JvmOverloads
    fun invokeWithPriority(removeAfterInvoking: Boolean = true, task: UITask) = addPrioritizedTask(
        Task(
            removeAfterInvoking,
            task
        )
    )

    @JvmOverloads
    operator fun invoke(removeAfterInvoking: Boolean = true, task: UITask) = addTask(Task(removeAfterInvoking, task))

    operator fun plusAssign(task: UITask) {
        invoke(task = task)
    }

    fun loadFont(font: String) = loadFont(nanovg, font)

    fun loadFont(font: Font) {
        loadFont(font.regular)
        font.bold?.let { loadFont(it) }
        font.italic?.let { loadFont(it) }
        font.italicBold?.let { loadFont(it) }
    }

    /**
     * Represents a UIElement that has been assigned to a UIManager.
     * Properties cannot be called until the UI is drawn for the first time.
     */
    open inner class Element internal constructor(private val element: UIElement) {
        val width: Float
            get() = element.width(nanovg)

        val height: Float
            get() = element.height(nanovg)

        /**
         * Add a task to draw this element on the provided location to the assigned manager.
         * @return was the task successfully added to the drawing queue?
         */
        infix fun at(location: Vector2fc): Element {
            invoke {
                element.draw(it, location)
            }

            return this
        }

        infix fun prioritizedAt(location: Vector2fc): Element {
            invokeWithPriority {
                element.draw(it, location)
            }

            return this
        }
    }

    data class Task(val removeAfterInvoking: Boolean, val task: UITask)
}