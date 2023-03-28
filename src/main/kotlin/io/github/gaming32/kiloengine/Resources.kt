@file:Suppress("unused")

package io.github.gaming32.kiloengine

import io.github.gaming32.kiloengine.util.plus
import java.io.InputStream

typealias ResourceGetter = (String) -> InputStream?

object Resources {
    @JvmStatic
    var resourceGetter: ResourceGetter = javaClass::getResourceAsStream

    @JvmStatic
    fun addResourceGetter(getter: ResourceGetter) {
        resourceGetter += getter
    }

    @JvmStatic
    fun addOverridingGetter(getter: ResourceGetter, vararg files: String) {
        val next = resourceGetter
        resourceGetter = { (if (it in files) getter else next)(it) }
    }

    @JvmStatic
    fun getResource(path: String) = resourceGetter(path)
}
