package io.github.gaming32.kiloengine.ui

import org.joml.Vector2fc
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG.*

interface AbstractBoxElement: UIElement {
    var backgroundColor: NVGColor?
    var radius: Float?

    override fun draw(nanovg: Long, location: Vector2fc) {
        if (backgroundColor == null) return


        nvgBeginPath(nanovg)
        nvgFillColor(nanovg, backgroundColor)
        val radius = radius

        if (radius == null) {
            nvgRect(nanovg, location.x(), location.y(), width(nanovg), height(nanovg))
        } else {
            nvgRoundedRect(nanovg, location.x(), location.y(), width(nanovg), height(nanovg), radius)
        }

        nvgFill(nanovg)
        nvgClosePath(nanovg)
    }

    override fun width(nanovg: Long): Float
    override fun height(nanovg: Long): Float
}