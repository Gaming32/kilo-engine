package io.github.gaming32.fungame.obj

import io.github.gaming32.fungame.model.Material
import io.github.gaming32.fungame.model.Model
import io.github.gaming32.fungame.util.simpleParentDir
import org.joml.Vector3f
import java.io.BufferedReader
import java.io.InputStream

class ObjLoader(private val getResource: (String) -> InputStream?) {
    fun loadObj(name: String) = textResource(name) { inp ->
        val parentDir = simpleParentDir(name)
        val verts = mutableListOf<Vector3f>()
        val normals = mutableListOf<Vector3f>()
        val uvs = mutableListOf<Model.UV>()
        val tris = mutableListOf<Model.Tri>()
        val materials = mutableMapOf<String, Material>()
        var material: Material? = null
        for (line in parseLines(inp)) {
            when (line[0]) {
                "mtllib" -> materials += loadMaterialLibrary(parentDir + line[1])
                "usemtl" -> material = materials[line[1]]
                "v" -> verts += Vector3f(
                    line[1].toFloat(),
                    line[2].toFloat(),
                    line[3].toFloat()
                )
                "vn" -> normals += Vector3f(
                    line[1].toFloat(),
                    line[2].toFloat(),
                    line[3].toFloat()
                )
                "vt" -> uvs += Model.UV(
                    line[1].toFloat(),
                    1f - line[2].toFloat()
                )
                "f" -> {
                    for (i in 2 until line.size - 1) {
                        tris += Model.Tri(
                            parseVertex(verts, normals, uvs, line[1]),
                            parseVertex(verts, normals, uvs, line[i]),
                            parseVertex(verts, normals, uvs, line[i + 1]),
                            material
                        )
                    }
                }
            }
        }
        Model(tris)
    } ?: throw IllegalArgumentException("Missing OBJ model: $name")

    private fun loadMaterialLibrary(name: String): Map<String, Material> = textResource(name) { inp ->
        val parentDir = simpleParentDir(name)
        val materials = mutableMapOf<String, Material>()
        var currentMaterial = ""
        for (line in parseLines(inp)) {
            when (line[0]) {
                "newmtl" -> currentMaterial = line[1]
                "Kd" -> if (materials[currentMaterial] !is Material.Texture) {
                    materials[currentMaterial] = Material.Color(
                        line[1].toFloat(), line[2].toFloat(), line[3].toFloat()
                    )
                }
                "map_Kd" -> materials[currentMaterial] = Material.Texture(parentDir + line[1])
            }
        }
        materials
    } ?: throw IllegalArgumentException("Missing material library: $name")

    private fun parseVertex(
        verts: List<Vector3f>,
        normals: List<Vector3f>,
        uvs: List<Model.UV>,
        vertex: String
    ): Model.Vertex {
        val parts = vertex.split("/")
        return Model.Vertex(
            verts[parts[0].toInt() - 1],
            if (parts.size > 2 && parts[2].isNotEmpty()) {
                normals[parts[2].toInt() - 1]
            } else {
                null
            },
            if (parts.size > 1 && parts[1].isNotEmpty()) {
                uvs[parts[1].toInt() - 1]
            } else {
                null
            }
        )
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
