package io.github.gaming32.fungame.util

import org.lwjgl.opengl.GL11.*
import java.lang.ref.Cleaner

class DisplayListDrawable(displayList: Int) : Drawable, AutoCloseable {
    private class DisplayList(val displayList: Int) : Runnable {
        var open = true

        override fun run() {
            if (open) {
                open = false
                glDeleteLists(displayList, 1)
            }
        }
    }

    companion object {
        private val CLEANER = Cleaner.create()
    }

    private val list = DisplayList(displayList)

    init {
        CLEANER.register(this, list)
    }

    private fun ensureOpen() {
        if (!list.open) {
            throw IllegalStateException("Tried to draw deleted display list ${list.displayList}")
        }
    }

    override fun draw() {
        ensureOpen()
        glCallList(list.displayList)
    }

    override fun close() = list.run()
}

inline fun buildDisplayList(builder: ModelBuilder.() -> Unit): DisplayListDrawable {
    val list = glGenLists(1)
    glNewList(list, GL_COMPILE)
    ModelBuilder().builder()
    glEndList()
    return DisplayListDrawable(list)
}
