package io.github.gaming32.kiloengine.ui

interface SizedUIElement : UIElement {
    var size : Float

    override fun height(nanovg: Long): Float = size
}