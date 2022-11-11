package io.github.gaming32.fungame.obj

import io.github.gaming32.fungame.util.DisplayListDrawable
import io.github.gaming32.fungame.util.ModelBuilder
import io.github.gaming32.fungame.util.buildDisplayList
import io.github.gaming32.fungame.util.simpleParentDir
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.GL_LINE_STRIP
import org.lwjgl.opengl.GL11.GL_POLYGON
import java.io.BufferedReader
import java.io.InputStream

class ObjLoader(private val getResource: (String) -> InputStream?) {
    fun loadObj(name: String): DisplayListDrawable {
        val parentDir = simpleParentDir(name)
        return buildDisplayList {
            textResource(name) { inp ->
                val vertices = mutableListOf<Vector3f>()
                val uvs = mutableListOf<Vector2f>()
                color(1f, 1f, 1f)
                for (line in parseLines(inp)) {
                    when (line[0]) {
                        "v" -> vertices += Vector3f(
                            line[1].toFloat(),
                            line[2].toFloat(),
                            line[3].toFloat()
                        )
                        "vt" -> uvs += Vector2f(
                            line[1].toFloat(),
                            line[2].toFloat()
                        )
                        "f" -> {
                            begin(GL_POLYGON)
                            for (i in 1 until line.size) {
                                parseVertex(vertices, uvs, line[i])
                            }
                            draw()
                        }
                        "l" -> {
                            begin(GL_LINE_STRIP)
                            for (i in 1 until line.size) {
                                vertex(vertices[line[i].toInt() - 1])
                            }
                            draw()
                        }
                    }
                }
            } ?: throw IllegalArgumentException("Missing OBJ model: $name")
        }
    }

    private fun ModelBuilder.parseVertex(vertices: List<Vector3f>, uvs: List<Vector2f>, vertex: String) {
        val parts = vertex.split("/")
        if (parts.size > 1 && parts[1].isNotEmpty()) {
            uv(uvs[parts[1].toInt() - 1])
        }
        vertex(vertices[parts[0].toInt() - 1])
    }

    private inline fun <T> textResource(
        name: String,
        action: (inp: BufferedReader) -> T
    ) = getResource(name)?.bufferedReader(Charsets.UTF_8)?.use(action)

    private fun parseLines(inp: BufferedReader) = inp.lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .filter { !it.startsWith("#") }
        .map { it.split(" ") }
}
