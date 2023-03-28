package io.github.gaming32.kiloengine.ui

import org.joml.Vector2fc

/**
 * Mutable
 */
interface UIElement {
    var label : String

    fun draw(nanovg: Long, location: Vector2fc)
    fun width(nanovg: Long) : Float
    fun height(nanovg: Long) : Float

    infix fun with(manager: UIManager) = manager.Element(this)

    /**
     * A more compact method for our non-decaf friends.
     * @see with
     * @see UIManager.Element.at
     */
    fun assignedAt(manager: UIManager, location: Vector2fc) = this with manager at location
}
