package io.github.gaming32.kiloengine.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.Resources
import org.lwjgl.nanovg.NanoVG.nvgCreateFontMem
import org.lwjgl.opengl.GL20.*
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
    Resources.getResource("/$name.ttf")?.use { it.copyTo(baos) }
    val memory = MemoryUtil.memAlloc(baos.size())
    try {
        memory.put(baos.toByteArray())
        memory.flip()
        return nvgCreateFontMem(nanovg, name, memory, 1)
    } finally {
        MemoryUtil.memFree(memory)
    }
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

inline fun <reified T> Any?.cast() = this as T

inline fun <reified T> Any?.castOrNull() = this as? T

val Class<*>.wrapperType get() = when (this) {
    Nothing::class.javaPrimitiveType -> Nothing::class.javaObjectType
    Byte::class.javaPrimitiveType -> Byte::class.javaObjectType
    Short::class.javaPrimitiveType -> Short::class.javaObjectType
    Char::class.javaPrimitiveType -> Char::class.javaObjectType
    Int::class.javaPrimitiveType -> Int::class.javaObjectType
    Float::class.javaPrimitiveType -> Float::class.javaObjectType
    Long::class.javaPrimitiveType -> Long::class.javaObjectType
    Double::class.javaPrimitiveType -> Double::class.javaObjectType
    else -> this
}

fun getShader(type: Int, file: String) = Resources.getResource(file)?.use {
    val shader = glCreateShader(type)
    glShaderSource(shader, it.reader().readText())
    glCompileShader(shader)
    if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
        throw RuntimeException(glGetShaderInfoLog(shader))
    }
    shader
} ?: throw IllegalArgumentException("Cannot find shader file $file")
