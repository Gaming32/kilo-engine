package io.github.gaming32.fungame.util

import org.lwjgl.nanovg.NanoVG.nvgCreateFontMem
import org.lwjgl.system.MemoryUtil
import java.io.ByteArrayOutputStream
import kotlin.math.PI

const val FPI = PI.toFloat()

fun simpleParentDir(path: String): String {
    val slash = path.lastIndexOf('/')
    if (slash == -1) {
        return ""
    }
    return path.substring(0, slash + 1)
}

fun CharSequence.count(c: Char) = count { it == c }

inline fun <T, R> withValue(value: T, get: () -> T, set: (T) -> Unit, action: () -> R): R {
    val old = get()
    set(value)
    val result = action()
    set(old)
    return result
}

fun loadFont(nanovg: Long, name: String): Int {
    val baos = ByteArrayOutputStream()
    object {}::class.java.getResourceAsStream("/$name.ttf")?.use { it.copyTo(baos) }
    val memory = MemoryUtil.memAlloc(baos.size())
    memory.put(baos.toByteArray())
    memory.flip()
    return nvgCreateFontMem(nanovg, name, memory, 1)
}

fun <K, V> Map<K, V>.invert(): Map<V, K> {
    val result = mutableMapOf<V, K>()
    forEach { (k, v) -> result[v] = k }
    return result
}
