package io.github.gaming32.kiloengine.ui

import org.lwjgl.nanovg.NVGColor

interface AbstractTextElement : UIElement {
    var size : Float
    var color: NVGColor

    override fun height(nanovg: Long): Float = size
}