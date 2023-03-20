package io.github.gaming32.kiloengine.loader

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.gaming32.gson5.Gson5Reader
import io.github.gaming32.kiloengine.ResourceGetter
import io.github.gaming32.kiloengine.Scene
import io.github.gaming32.kiloengine.entity.ComponentRegistry
import io.github.gaming32.kiloengine.entity.Entity
import io.github.gaming32.kiloengine.mesh.Material
import io.github.gaming32.kiloengine.mesh.Mesh
import io.github.gaming32.kiloengine.mesh.Model
import io.github.gaming32.kiloengine.util.simpleParentDir
import io.github.gaming32.kiloengine.util.toDVector3
import org.joml.Vector3f
import org.ode4j.math.DVector3
import org.quiltmc.json5.JsonReader
import java.io.BufferedReader

class SceneLoaderImpl(private val resourceGetter: () -> ResourceGetter) : SceneLoader {
    override fun loadOBJ(name: String) = textResource(name) { inp, parentDir ->
        val verts = mutableListOf<Vector3f>()
        val vertColors = mutableListOf<Material.Color>()
        val normals = mutableListOf<Vector3f>()
        val uvs = mutableListOf<Mesh.UV>()
        val trises = mutableListOf<Mesh.Triangle>()
        val materials = mutableMapOf<String, Material>()
        var material: Material? = null
        for (line in parseLines(inp)) {
            when (line[0]) {
                "mtllib" -> materials += loadMaterialLibrary(parentDir + line[1])
                "usemtl" -> material = materials[line[1]]
                "v" -> {
                    verts += Vector3f(
                        line[1].toFloat(),
                        line[2].toFloat(),
                        line[3].toFloat()
                    )
                    vertColors += if (line.size > 4) {
                        Material.Color(
                            line[4].toFloat(),
                            line[4].toFloat(),
                            line[4].toFloat()
                        )
                    } else {
                        Material.Color.DEFAULT
                    }
                }
                "vn" -> normals += Vector3f(
                    line[1].toFloat(),
                    line[2].toFloat(),
                    line[3].toFloat()
                )
                "vt" -> uvs += Mesh.UV(
                    line[1].toFloat(),
                    1f - line[2].toFloat()
                )
                "f" -> {
                    for (i in 2 until line.size - 1) {
                        trises += Mesh.Triangle(
                            parseVertex(verts, vertColors, normals, uvs, line[1]),
                            parseVertex(verts, vertColors, normals, uvs, line[i]),
                            parseVertex(verts, vertColors, normals, uvs, line[i + 1]),
                            material
                        )
                    }
                }
            }
        }
        Model(trises, materials)
    } ?: throw IllegalArgumentException("Missing OBJ mesh: $name")

    override fun loadMaterialLibrary(name: String) = textResource(name) { inp, parentDir ->
        val materials = mutableMapOf<String, Material>()
        var currentMaterial = ""
        for (line in parseLines(inp)) {
            when (line[0]) {
                "newmtl" -> currentMaterial = line[1]
                "Kd" -> if (materials[currentMaterial] !is Material.Texture) {
                    materials[currentMaterial] = Material.Color(
                        line[1].toFloat(), line[2].toFloat(), line[3].toFloat(), line.getOrNull(4)?.toFloat() ?: 1f
                    )
                }
                "map_Kd" -> materials[currentMaterial] = Material.Texture(
                    if (line[1].startsWith("~")) {
                        line[1]
                    } else {
                        parentDir + line[1]
                    }
                )
                "d" -> materials[currentMaterial].let { oldMat ->
                    val opacity = line[1].toFloat()
                    materials[currentMaterial] = when (oldMat) {
                        null -> Material.Color(1f, 1f, 1f, opacity)
                        is Material.Color -> oldMat.copy(a = opacity)
                        is Material.Texture -> oldMat.copy(color = oldMat.color.copy(a = opacity))
                    }
                }
            }
        }
        materials
    } ?: throw IllegalArgumentException("Missing material library: $name")

    override fun loadScene(name: String, scene: Scene) = textResource(name) { inp, _ ->
        val json = JsonParser.parseReader(Gson5Reader(JsonReader.json5(inp))).asJsonObject
        json["gravity"]?.asJsonArray?.let { scene.world.setGravity(it.toDVector3()) }
        json["entities"]?.asJsonArray?.forEach { entityData ->
            entityData as JsonObject
            val position = entityData.remove("position")?.asJsonArray?.toDVector3() ?: DVector3()
            val kinematic = entityData.remove("kinematic")?.asBoolean ?: false
            val componentsData = entityData.remove("components")?.asJsonArray?.asList() ?: listOf()
            val entity = Entity(scene, position)
            if (kinematic) {
                entity.body.setKinematic()
            }
            for (componentData in componentsData) {
                if (componentData.isJsonPrimitive) {
                    ComponentRegistry.getType(componentData.asString)
                        .create(entity, this, JsonObject())
                } else {
                    componentData as JsonObject
                    ComponentRegistry.getType(componentData.remove("type").asString)
                        .create(entity, this, componentData)
                }
            }
        }
        scene
    } ?: throw IllegalArgumentException("Missing scene: $name")

    private fun parseVertex(
        verts: List<Vector3f>,
        vertColors: List<Material.Color>,
        normals: List<Vector3f>,
        uvs: List<Mesh.UV>,
        vertex: String
    ): Mesh.Vertex {
        val parts = vertex.split("/")
        val vertIndex = parts[0].toInt() - 1
        return Mesh.Vertex(
            verts[vertIndex],
            if (parts.size > 2 && parts[2].isNotEmpty()) {
                normals[parts[2].toInt() - 1]
            } else {
                null
            },
            if (parts.size > 1 && parts[1].isNotEmpty()) {
                uvs[parts[1].toInt() - 1]
            } else {
                null
            },
            vertColors[vertIndex]
        )
    }

    private inline fun <T> textResource(
        name: String,
        action: (inp: BufferedReader, parentDir: String) -> T
    ) = resourceGetter()(name)?.bufferedReader(Charsets.UTF_8)?.use { action(it, simpleParentDir(name)) }

    private fun parseLines(inp: BufferedReader) = inp.lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .filter { !it.startsWith("#") }
        .map { it.split(" ") }
}
