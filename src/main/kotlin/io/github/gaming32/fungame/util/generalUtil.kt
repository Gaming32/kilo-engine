package io.github.gaming32.fungame.util

import kotlin.math.PI

const val FPI = PI.toFloat()

fun simpleParentDir(path: String): String {
    val slash = path.indexOf('/')
    if (slash == -1) {
        return ""
    }
    return path.substring(0, slash + 1)
}

fun CharSequence.count(c: Char) = count { it == c }
