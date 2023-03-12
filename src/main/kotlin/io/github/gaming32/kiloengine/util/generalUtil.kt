package io.github.gaming32.kiloengine.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.lwjgl.nanovg.NanoVG.nvgCreateFontMem
import org.lwjgl.system.MemoryUtil
import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.abs

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

fun Double.fuzzyEquals(other: Double, delta: Double) = abs(other - this) <= delta

infix fun Double.fuzzyEquals(other: Double) = fuzzyEquals(other, 1e-7)

fun normalizeDegrees(angle: Double): Double {
    var result = angle
    while (result <= -180) {
        result += 360
    }
    while (result > 180) {
        result -= 360
    }
    return result
}

fun normalizeDegrees(angle: Float): Float {
    var result = angle
    while (result <= -180) {
        result += 360
    }
    while (result > 180) {
        result -= 360
    }
    return result
}

fun JsonObject.getElement(name: String): JsonElement = asMap().getValue(name)

fun unreachable(): Nothing {
    throw IllegalStateException("Shouldn't reach here")
}

operator fun <T, R> ((T) -> R?).plus(other: (T) -> R?): (T) -> R? = { invoke(it) ?: other(it) }
