package io.github.gaming32.kiloengine

import io.github.gaming32.kiloengine.entity.CameraComponent
import io.github.gaming32.kiloengine.entity.PlayerComponent
import io.github.gaming32.kiloengine.loader.SceneLoader
import io.github.gaming32.kiloengine.loader.SceneLoaderImpl
import io.github.gaming32.kiloengine.util.*
import org.joml.*
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS
import org.lwjgl.nanovg.NanoVGGL3.nvgCreate
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL13.GL_MULTISAMPLE
import org.lwjgl.opengl.GL30.*
import org.ode4j.math.DVector3
import org.ode4j.ode.DContact.DSurfaceParameters
import org.ode4j.ode.DContactGeomBuffer
import org.ode4j.ode.OdeHelper
import kotlin.math.roundToLong

abstract class KiloEngineGame {
    companion object {
        private const val PHYSICS_SPEED = 0.02
        private const val CONTACT_COUNT = 16
        private const val VERTICAL_DAMPING = 0.01
        private const val HORIZONTAL_DAMPING = 0.17
        val SURFACE_PARAMS = DSurfaceParameters().apply {
            mu = 0.0
        }
        val WALL_PARAMS = DSurfaceParameters().apply {
            mu = 3.0
        }

        @JvmField
        val EDITOR_MODE = System.getProperty("kilo.editor").toBoolean()
        val EDITOR_DEBUG_COLOR = Vector3f(50 / 255f, 168 / 255f, 113 / 255f)
        private const val EDITOR_MOVE_SPEED = 0.35f
        private const val EDITOR_LOOK_SPEED = 0.2f
    }

    init {
        OdeHelper.initODE()
    }

    val scene = Scene()
    private val windowSize = Vector2i()
    private val movementInput = Vector3d()
    var wireframe = false
    private var clearParams = GL_DEPTH_BUFFER_BIT
    private val matrices = MatrixStacks(16)
    lateinit var sceneLoader: SceneLoader
        private set

    private val editorCameraPos = Vector3f()
    private val editorCameraRot = Vector2f()

    // Handles
    private var window = 0L
    private var nanovg = 0L
    private var vao = 0
    private var vbo = 0
    private var vertexShader = 0
    private var fragmentShader = 0
    private var shaderProgram = 0

    fun main() {
        init()
        registerEvents()
        loadInitScene()
        val cubemapSkybox = scene.skybox.castOrNull<Skybox.Cubemap>()?.let {
            withValue(-1, TextureManager::maxMipmap, { TextureManager.maxMipmap = it }) {
                withValue(GL_NEAREST, TextureManager::filter, { TextureManager.filter = it }) {
                    TextureManager.loadAsVirtual(it.down, "skybox/down")
                    TextureManager.loadAsVirtual(it.up, "skybox/up")
                    TextureManager.loadAsVirtual(it.negativeZ, "skybox/negativeZ")
                    TextureManager.loadAsVirtual(it.positiveZ, "skybox/positiveZ")
                    TextureManager.loadAsVirtual(it.negativeX, "skybox/negativeX")
                    TextureManager.loadAsVirtual(it.positiveX, "skybox/positiveX")
                }
            }
            sceneLoader.loadObj("/skybox.obj").toDisplayList()
        }
        scene.skybox.castOrNull<Skybox.SolidColor>()?.let {
            glClearColor(it.r, it.g, it.b, 1f)
            clearParams = GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT
        }
        var lastTime = glfwGetTime()
        var lastPhysicsTime = lastTime
        var fpsAverage = 0.0
        if (EDITOR_MODE) {
            scene.getComponentOrNull<PlayerComponent>()?.let {
                editorCameraPos.set(it.entity.body.position.toVector3f())
                editorCameraRot.set(it.rotation)
            }
        }
        val contactJointGroup = OdeHelper.createJointGroup()
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents()

            val time = glfwGetTime()
            val deltaTime = time - lastTime
            val fps = 1 / deltaTime
            fpsAverage = 0.95 * fpsAverage + 0.05 * fps
            lastTime = time

            if (time - lastPhysicsTime > 1.0) {
                lastPhysicsTime = time - 1.0
            }
            while (time - lastPhysicsTime > PHYSICS_SPEED) {
                val mouseLocked = glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
                if (EDITOR_MODE) {
                    if (mouseLocked) {
                        editorCameraPos += movementInput.toVector3f()
                            .rotateX(Math.toRadians(editorCameraRot.x))
                            .rotateY(Math.toRadians(editorCameraRot.y))
                            .mul(EDITOR_MOVE_SPEED)
                    }
                    movementInput.y = 0.0
                } else {
                    scene.invokeEvent(EventType.PRE_TICK)
                    if (mouseLocked) {
                        scene.invokeEvent(EventType.HANDLE_MOVEMENT, movementInput)
                    }
                    movementInput.y = 0.0
                    scene.entities.forEach { entity ->
                        entity.body.addForce(
                            entity.body.linearVel.x * -HORIZONTAL_DAMPING / PHYSICS_SPEED,
                            entity.body.linearVel.y * -VERTICAL_DAMPING / PHYSICS_SPEED,
                            entity.body.linearVel.z * -HORIZONTAL_DAMPING / PHYSICS_SPEED
                        )
                    }
                    val contactBuffer = DContactGeomBuffer(CONTACT_COUNT)
                    contactJointGroup.clear()
                    scene.space.collide(null) { _, o1, o2 ->
                        repeat(OdeHelper.collide(o1, o2, CONTACT_COUNT, contactBuffer)) {
                            val contact = contactBuffer[it]
                            val e1 = scene.getEntityByBody(contact.g1.body)
                            val e2 = scene.getEntityByBody(contact.g2.body)
                            if (e1 == null || e2 == null) return@repeat
                            var surfaceParams = e1.collideWithEntity(e2, contact, true)
                            val surfaceParams2 = e2.collideWithEntity(e1, contact, false)
                            surfaceParams = surfaceParams ?: surfaceParams2
                            if (surfaceParams == null) return@repeat
                            val joint = OdeHelper.createContactJoint(
                                scene.world,
                                contactJointGroup,
                                DContact(contact, surfaceParams)
                            )
                            joint.attach(contact.g1.body, contact.g2.body)
                        }
                    }
                    scene.world.quickStep(PHYSICS_SPEED)
                    scene.invokeEvent(EventType.TICK)
                }
                lastPhysicsTime += PHYSICS_SPEED
            }

            // 3D Mode
            glEnable(GL_CULL_FACE)

            if (EDITOR_MODE) {
                glBindFramebuffer(GL_FRAMEBUFFER, 0)
                glViewport(0, 0, windowSize.x, windowSize.y)

                matrices.projection.clear()
                matrices.projection.perspective(
                    Math.toRadians(80f),
                    windowSize.x / windowSize.y.toFloat(),
                    0.01f, 1000f
                )

                glClear(clearParams)

                matrices.model.clear()
                matrices.model.rotateX(Math.toRadians(editorCameraRot.x))
                matrices.model.rotateY(Math.toRadians(180 - editorCameraRot.y))

                if (cubemapSkybox != null) {
                    glDisable(GL_DEPTH_TEST)
                    cubemapSkybox.draw(matrices)
                    glClear(GL_DEPTH_BUFFER_BIT)
                }

                glEnable(GL_DEPTH_TEST)
                matrices.model.translate(-editorCameraPos.x, -editorCameraPos.y, -editorCameraPos.z)
                scene.invokeEvent(EventType.DRAW, matrices)
            } else {
                for (camera in scene.getComponents<CameraComponent>()) {
                    val aspect: Float
                    if (camera.renderArea != null) {
                        glBindFramebuffer(GL_FRAMEBUFFER, 0)
                        val w = (camera.renderArea.second.x - camera.renderArea.first.x) * windowSize.x
                        val h = (camera.renderArea.second.y - camera.renderArea.first.y) * windowSize.y
                        glViewport(
                            (camera.renderArea.first.x * windowSize.x).toInt(),
                            (camera.renderArea.first.y * windowSize.y).toInt(),
                            w.toInt(), h.toInt()
                        )
                        aspect = w / h
                    } else if (camera.textureResolution != null) {
                        glViewport(0, 0, camera.textureResolution.x, camera.textureResolution.y)
                        glBindFramebuffer(GL_FRAMEBUFFER, camera.framebuffer)
                        aspect = camera.textureResolution.x.toFloat() / camera.textureResolution.y
                    } else {
                        unreachable()
                    }
                    matrices.projection.clear()
                    if (camera.fov == null) {
                        matrices.projection.ortho(
                            camera.orthoRange.first.x.toFloat(),
                            camera.orthoRange.second.x.toFloat(),
                            camera.orthoRange.first.y.toFloat(),
                            camera.orthoRange.second.y.toFloat(),
                            0.01f, 1000f
                        )
                    } else {
                        matrices.projection.perspective(Math.toRadians(camera.fov!!), aspect, 0.01f, 1000f)
                    }
                    glClear(clearParams)

                    // Rotate camera
                    matrices.model.clear()
                    matrices.model.rotateX(Math.toRadians(camera.rotation.x))
                    matrices.model.rotateZ(Math.toRadians(camera.rotation.z))
                    matrices.model.rotateY(Math.toRadians(camera.rotation.y))

                    // Skybox
                    if (cubemapSkybox != null) {
                        glDisable(GL_DEPTH_TEST)

                        if (camera.fov != null) {
                            cubemapSkybox.draw(matrices)
                        }

                        glClear(GL_DEPTH_BUFFER_BIT)
                    }

                    // Scene
                    glEnable(GL_DEPTH_TEST)
                    val position = DVector3(camera.entity.body.position).add(camera.offset)
                    matrices.model.translate(-position.x.toFloat(), -position.y.toFloat(), -position.z.toFloat())
                    scene.invokeEvent(EventType.DRAW, matrices)
                }
            }

            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            glViewport(0, 0, windowSize.x, windowSize.y)

            // HUD
            matrices.projection.clear()
            matrices.projection.ortho(0f, windowSize.x.toFloat(), windowSize.y.toFloat(), 0f, 1f, 3f)
            matrices.model.clear()
            matrices.model.translate(0f, 0f, -2f)
            glDisable(GL_DEPTH_TEST)

            val widthArray = IntArray(1)
            glfwGetFramebufferSize(window, widthArray, null)
            nvgBeginFrame(
                nanovg,
                windowSize.x.toFloat(), windowSize.y.toFloat(),
                widthArray[0].toFloat() / windowSize.x
            )
            nvgFontFace(nanovg, "minecraftia")
            nvgText(nanovg, 10f, 35f, "FPS: ${fpsAverage.roundToLong()}")
            scene.invokeEvent(EventType.DRAW_UI, nanovg)
            nvgEndFrame(nanovg)

            glUseProgram(shaderProgram)
            glBindVertexArray(vao)
            glBindBuffer(GL_ARRAY_BUFFER, vbo)

            glfwSwapBuffers(window)
        }
        cubemapSkybox?.close()
        contactJointGroup.destroy()
        quit()
    }

    private fun init() {
        GLFWErrorCallback.createPrint().set()
        if (!glfwInit()) {
            throw Exception("Failed to initialize GLFW")
        }

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
//        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
        glfwWindowHint(GLFW_SAMPLES, 4)
//        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE)

//        val monitor = glfwGetPrimaryMonitor()
//        val videoMode = glfwGetVideoMode(monitor) ?: throw Exception("Could not determine video mode")

        windowSize.set(1280, 720)
        window = glfwCreateWindow(windowSize.x, windowSize.y, title, 0, 0)
        glfwMakeContextCurrent(window)
        GL.createCapabilities()
//        GLUtil.setupDebugMessageCallback()

        nanovg = nvgCreate(NVG_ANTIALIAS)
        loadFont(nanovg, "minecraftia")

        glfwSwapInterval(1)

        glfwSetWindowSizeCallback(window) { _, width, height ->
            windowSize.set(width, height)
        }

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_MULTISAMPLE)

        glClearColor(0f, 0f, 0f, 1f)
        glLineWidth(10f)

        vao = glGenVertexArrays()
        glBindVertexArray(vao)

        vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        vertexShader = getShader(GL_VERTEX_SHADER, "/shader.vert")
        fragmentShader = getShader(GL_FRAGMENT_SHADER, "/texture.frag")

        shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, fragmentShader)
        glBindFragDataLocation(shaderProgram, 0, "fragColor")
        glLinkProgram(shaderProgram)

        glGetProgrami(shaderProgram, GL_LINK_STATUS).let {
            if (it != GL_TRUE) {
                throw RuntimeException(glGetProgramInfoLog(shaderProgram))
            }
        }
        glUseProgram(shaderProgram)

        matrices.uniModel = glGetUniformLocation(shaderProgram, "model")
        matrices.uniProjection = glGetUniformLocation(shaderProgram, "projection")

        glGetAttribLocation(shaderProgram, "position").let {
            glEnableVertexAttribArray(it)
            glVertexAttribPointer(it, 3, GL_FLOAT, false, 11 * 4, 0)
        }

        glGetAttribLocation(shaderProgram, "texcoord").let {
            glEnableVertexAttribArray(it)
            glVertexAttribPointer(it, 2, GL_FLOAT, false, 11 * 4, 6 * 4)
        }

        glGetAttribLocation(shaderProgram, "color").let {
            glEnableVertexAttribArray(it)
            glVertexAttribPointer(it, 3, GL_FLOAT, false, 11 * 4, 8 * 4)
        }

        glUniform1i(glGetUniformLocation(shaderProgram, "texImage"), 0)

        sceneLoader = SceneLoaderImpl(Resources::resourceGetter)
    }

    private fun registerEvents() {
        val lastMousePos = Vector2d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        glfwSetCursorPosCallback(window) { _, x, y ->
            if (
                lastMousePos.x != Double.POSITIVE_INFINITY &&
                glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
            ) {
                val event = MouseMoveEvent(x, y, x - lastMousePos.x, y - lastMousePos.y)
                if (EDITOR_MODE) {
                    editorCameraRot.y = normalizeDegrees(
                        editorCameraRot.y - (event.relX * EDITOR_LOOK_SPEED).toFloat()
                    )
                    editorCameraRot.x = Math.clamp(
                        -90f, 90f,
                        editorCameraRot.x + (event.relY * EDITOR_LOOK_SPEED).toFloat()
                    )
                } else {
                    scene.invokeEvent(EventType.MOUSE_MOVED, event)
                }
            }
            lastMousePos.set(x, y)
        }

        glfwSetMouseButtonCallback(window) { _, _, action, _ ->
            if (action == GLFW_PRESS) {
                if (EDITOR_MODE) {
                } else {
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
                }
            }
        }

        glfwSetKeyCallback(window) { _, key, _, action, _ ->
            if (action == GLFW_REPEAT) return@glfwSetKeyCallback
            val press = action == GLFW_PRESS
            if (press && EDITOR_MODE && key == GLFW_KEY_SPACE) {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
            }
            val mouseLocked = glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
            when (key) {
                GLFW_KEY_ESCAPE -> if (press && !EDITOR_MODE) {
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
                    movementInput.x = 0.0
                    movementInput.z = 0.0
                }
                GLFW_KEY_W -> if (press) {
                    movementInput.z++
                } else {
                    movementInput.z--
                }
                GLFW_KEY_S -> if (press) {
                    movementInput.z--
                } else {
                    movementInput.z++
                }
                GLFW_KEY_A -> if (press) {
                    movementInput.x++
                } else {
                    movementInput.x--
                }
                GLFW_KEY_D -> if (press) {
                    movementInput.x--
                } else {
                    movementInput.x++
                }
                GLFW_KEY_SPACE -> if (EDITOR_MODE) {
                    if (!press) {
                        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
                        movementInput.x = 0.0
                        movementInput.z = 0.0
                    }
                } else if (press && mouseLocked) {
                    movementInput.y = 1.0
                }
                GLFW_KEY_F3 -> if (press && (mouseLocked || EDITOR_MODE)) {
                    wireframe = !wireframe
                    @Suppress("LiftReturnOrAssignment")
                    if (wireframe) {
                        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                        glLineWidth(2f)
                        clearParams = GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT
                    } else {
                        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
                        glLineWidth(10f)
                        clearParams = GL_DEPTH_BUFFER_BIT or if (scene.skybox is Skybox.SolidColor) {
                            GL_COLOR_BUFFER_BIT
                        } else {
                            0
                        }
                    }
                }
            }
        }
    }

    private fun quit() {
        scene.destroy()
        OdeHelper.closeODE()
        TextureManager.unload()
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
        glDeleteProgram(shaderProgram)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    abstract val title: String

    abstract fun loadInitScene()
}
